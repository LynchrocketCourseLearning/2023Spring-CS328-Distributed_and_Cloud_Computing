/**
 * Matrix multiplication with OpenMPI version 1
 * In this version, it just uses MPI_Send and MPI_Recv to distribute the segments of matrix and gather the calculation result.
 * The residual matrix segment will be all distributed to the last worker.
 */

#include <mpi.h>
#include <stdio.h>

#define MAT_SIZE 500
#define ROOT 0
#define EPS 1e-6
#define fabs(a) (((a) > 0) ? (a) : -(a))

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
       redundant_rows, /* the redundant rows */
       offset;         /* offset of rows */

   /* You need to intialize MPI here */
   MPI_Init(&argc, &argv);
   MPI_Comm_size(MPI_COMM_WORLD, &mpiSize);
   MPI_Comm_rank(MPI_COMM_WORLD, &rank);

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
      double start = MPI_Wtime();

      /* retrieve some parameters */
      worker_num = mpiSize - 1;
      rows = MAT_SIZE / worker_num;
      redundant_rows = MAT_SIZE % worker_num;

      /* Send matrix data to the worker tasks */
      for (int dest = 1; dest <= worker_num; ++dest)
      {
         if (dest == worker_num)
            rows += redundant_rows; // the last worker will receive redundant rows apart of rows
         MPI_Send(&offset, 1, MPI_INT, dest, 0, MPI_COMM_WORLD);
         MPI_Send(&rows, 1, MPI_INT, dest, 0, MPI_COMM_WORLD);
         MPI_Send(&a[offset][0], rows * MAT_SIZE, MPI_DOUBLE, dest, 0, MPI_COMM_WORLD);
         MPI_Send(&b, MAT_SIZE * MAT_SIZE, MPI_DOUBLE, dest, 0, MPI_COMM_WORLD);
         offset += rows;
      }

      /* Receive results from worker tasks */
      for (int src = 1; src <= worker_num; ++src)
      {
         MPI_Recv(&offset, 1, MPI_INT, src, 1, MPI_COMM_WORLD, MPI_STATUS_IGNORE);
         MPI_Recv(&rows, 1, MPI_INT, src, 1, MPI_COMM_WORLD, MPI_STATUS_IGNORE);
         MPI_Recv(&c[offset][0], rows * MAT_SIZE, MPI_DOUBLE, src, 1, MPI_COMM_WORLD, MPI_STATUS_IGNORE);
      }

      /* Measure finish time */
      double finish = MPI_Wtime();
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
   else
   {
      /* worker */
      /* Receive data from master and compute, then send back to master */
      MPI_Recv(&offset, 1, MPI_INT, ROOT, 0, MPI_COMM_WORLD, MPI_STATUS_IGNORE);
      MPI_Recv(&rows, 1, MPI_INT, ROOT, 0, MPI_COMM_WORLD, MPI_STATUS_IGNORE);
      MPI_Recv(&a, rows * MAT_SIZE, MPI_DOUBLE, ROOT, 0, MPI_COMM_WORLD, MPI_STATUS_IGNORE);
      MPI_Recv(&b, MAT_SIZE * MAT_SIZE, MPI_DOUBLE, ROOT, 0, MPI_COMM_WORLD, MPI_STATUS_IGNORE);

      for (int i = 0; i < rows; ++i)
      {
         for (int j = 0; j < MAT_SIZE; ++j)
         {
            c[i][j] = 0;
            for (int k = 0; k < MAT_SIZE; ++k)
               c[i][j] += a[i][k] * b[k][j];
         }
      }

      MPI_Send(&offset, 1, MPI_INT, ROOT, 1, MPI_COMM_WORLD);
      MPI_Send(&rows, 1, MPI_INT, ROOT, 1, MPI_COMM_WORLD);
      MPI_Send(&c, rows * MAT_SIZE, MPI_DOUBLE, ROOT, 1, MPI_COMM_WORLD);
   }

   /* Don't forget to finalize your MPI application */
   MPI_Finalize();

   return 0;
}