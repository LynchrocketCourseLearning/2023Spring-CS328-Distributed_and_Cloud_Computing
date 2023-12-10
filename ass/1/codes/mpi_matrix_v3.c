/**
 * Matrix multiplication with OpenMPI version 3
 * In this version, it uses MPI_Scatterv and MPI_Gattherv to distribute the segments of matrix and gather the calculation result.
 * All the residual matrix segments will be distributed to the workers as equal as possible.
 */

#include <mpi.h>
#include <stdio.h>
#include <stdlib.h>

#define MAT_SIZE 500
#define ROOT 0
#define EPS 1e-6
#define fabs(a) (((a) > 0) ? (a) : -(a))
#define min(a, b) (((a) > (b)) ? (b) : (a))

void brute_force_matmul(double mat1[MAT_SIZE][MAT_SIZE],
                        double mat2[MAT_SIZE][MAT_SIZE],
                        double res[MAT_SIZE][MAT_SIZE])
{
   /* matrix multiplication of mat1 and mat2, store the result in res */
   for (int i = 0; i < MAT_SIZE; ++i)
   {
      for (int j = 0; j < MAT_SIZE; ++j)
      {
         res[i][j] = 0;
         for (int k = 0; k < MAT_SIZE; ++k)
         {
            res[i][j] += mat1[i][k] * mat2[k][j];
         }
      }
   }
}

int main(int argc, char *argv[])
{
   int rank;
   int mpiSize;
   double a[MAT_SIZE][MAT_SIZE],  /* matrix A to be multiplied */
       b[MAT_SIZE][MAT_SIZE],     /* matrix B to be multiplied */
       c[MAT_SIZE][MAT_SIZE],     /* result matrix C */
       bfRes[MAT_SIZE][MAT_SIZE]; /* brute force result bfRes */

   int worker_num,     /* number of workers */
       rows,           /* number of rows for a worker */
       redundant_rows; /* the redundant rows */

   double start, finish; /* timer */

   int *sendcounts, *displs;

   /* You need to intialize MPI here */
   MPI_Init(&argc, &argv);
   MPI_Comm_size(MPI_COMM_WORLD, &mpiSize);
   MPI_Comm_rank(MPI_COMM_WORLD, &rank);

   /* retrieve some parameters */
   worker_num = mpiSize;
   rows = MAT_SIZE / worker_num;
   redundant_rows = MAT_SIZE % worker_num;
   sendcounts = (int *)malloc(sizeof(int) * worker_num);
   displs = (int *)malloc(sizeof(int) * worker_num);
   int iter_cnt = min(worker_num, redundant_rows);
   int offset = 0;
   for (int i = 0; i < iter_cnt; i++)
   {
      displs[i] = offset;
      sendcounts[i] = (rows + 1) * MAT_SIZE;
      offset += sendcounts[i];
   }
   if (iter_cnt < worker_num)
   {
      for (int i = iter_cnt; i < worker_num; i++)
      {
         displs[i] = offset;
         sendcounts[i] = rows * MAT_SIZE;
         offset += sendcounts[i];
      }
   }

   if (rank == ROOT)
   {
      /* master */
      /* First, fill some numbers into the matrix */
      for (int i = 0; i < MAT_SIZE; i++)
         for (int j = 0; j < MAT_SIZE; j++)
            a[i][j] = i + j;
      for (int i = 0; i < MAT_SIZE; i++)
         for (int j = 0; j < MAT_SIZE; j++)
            b[i][j] = i * j;

      /* Measure start time */
      start = MPI_Wtime();
   }

   /* Send matrix data to the worker tasks */
   MPI_Bcast(&b[0][0], MAT_SIZE * MAT_SIZE, MPI_DOUBLE, ROOT, MPI_COMM_WORLD);                                                                 // broadcast matrix b
   MPI_Scatterv(&a[0][0], sendcounts, displs, MPI_DOUBLE, &a[displs[rank] / MAT_SIZE][0], sendcounts[rank], MPI_DOUBLE, ROOT, MPI_COMM_WORLD); // scatter parts of matrix a to workers,

   /* worker */
   /* master itself as a worker */
   int st_offset = displs[rank] / MAT_SIZE, ed_offset = (displs[rank] + sendcounts[rank]) / MAT_SIZE;
   for (int i = st_offset; i < ed_offset; ++i)
   {
      for (int j = 0; j < MAT_SIZE; ++j)
      {
         c[i][j] = 0;
         for (int k = 0; k < MAT_SIZE; ++k)
         {
            c[i][j] += a[i][k] * b[k][j];
         }
      }
   }

   /* Receive results from worker tasks */
   MPI_Gatherv(&c[st_offset][0], (ed_offset - st_offset) * MAT_SIZE, MPI_DOUBLE, &c[0][0], sendcounts, displs, MPI_DOUBLE, ROOT, MPI_COMM_WORLD); // gather the data from workers

   if (rank == ROOT)
   {
      /* Measure finish time */
      finish = MPI_Wtime();
      printf("Done in %f seconds.\n", finish - start);

      /* Compare results with those from brute force */
      brute_force_matmul(a, b, bfRes);
      int flag = 1;
      for (int i = 0; i < MAT_SIZE; ++i)
      {
         for (int j = 0; j < MAT_SIZE; ++j)
         {
            if (fabs(c[i][j] - bfRes[i][j]) > EPS)
            {
               flag = 0;
               printf("%d %d %.20lf %.20lf\n", i, j, c[i][j], bfRes[i][j]);
               break;
            }
         }
         if (!flag)
            break;
      }
      printf((flag) ? "Correct\n" : "Wrong\n");
   }

   /* Don't forget to finalize your MPI application */
   free(sendcounts);
   free(displs);

   MPI_Finalize();

   return 0;
}