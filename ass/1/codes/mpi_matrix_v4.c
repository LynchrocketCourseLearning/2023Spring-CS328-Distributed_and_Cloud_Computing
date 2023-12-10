/**
 * Matrix multiplication with OpenMPI version 4
 * In this version, it uses MPI_Scatterv and MPI_Gattherv to distribute the segments of matrix and gather the calculation result.
 * The matrix segment sent will be the multiplication of 4.
 */

#include <mpi.h>
#include <stdio.h>
#include <stdlib.h>

#define MAT_SIZE 500
#define ROOT 0
#define EPS 1e-6
#define fabs(a) (((a) > 0) ? (a) : -(a))
#define min(a, b) (((a) > (b)) ? (b) : (a))
// in column-major order
#define A(i, j) a[(i) + (j)*MAT_SIZE]
#define B(i, j) b[(i) + (j)*MAT_SIZE]
#define C(i, j) c[(i) + (j)*MAT_SIZE]

const int mul_of_4[] = {0, 4, 8, 12, 16, 20, 24, 28, 32, 36, 40, 44, 48, 52, 56, 60, 64, 68, 72, 76, 80, 84, 88, 92, 96, 100, 104, 108, 112, 116, 120, 124, 128, 132, 136, 140, 144, 148, 152, 156, 160, 164, 168, 172, 176, 180, 184, 188, 192, 196, 200, 204, 208, 212, 216, 220, 224, 228, 232, 236, 240, 244, 248, 252, 256, 260, 264, 268, 272, 276, 280, 284, 288, 292, 296, 300, 304, 308, 312, 316, 320, 324, 328, 332, 336, 340, 344, 348, 352, 356, 360, 364, 368, 372, 376, 380, 384, 388, 392, 396, 400, 404, 408, 412, 416, 420, 424, 428, 432, 436, 440, 444, 448, 452, 456, 460, 464, 468, 472, 476, 480, 484, 488, 492, 496, 500};

void brute_force_matmul(double mat1[MAT_SIZE * MAT_SIZE],
                        double mat2[MAT_SIZE * MAT_SIZE],
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
            res[i][j] += mat1[i * MAT_SIZE + k] * mat2[k * MAT_SIZE + j];
         }
      }
   }
}

int main(int argc, char *argv[])
{
   int rank;
   int mpiSize;
   double a[MAT_SIZE * MAT_SIZE], /* matrix A to be multiplied */
       b[MAT_SIZE * MAT_SIZE],    /* matrix B to be multiplied */
       c[MAT_SIZE * MAT_SIZE],    /* result matrix C */
       bfRes[MAT_SIZE][MAT_SIZE]; /* brute force result bfRes */

   int worker_num; /* number of workers */

   double start, finish; /* timer */

   int *sendcounts, *displs;

   /* You need to intialize MPI here */
   MPI_Init(&argc, &argv);
   MPI_Comm_size(MPI_COMM_WORLD, &mpiSize);
   MPI_Comm_rank(MPI_COMM_WORLD, &rank);

   /* retrieve some parameters */
   worker_num = mpiSize;
   sendcounts = (int *)malloc(sizeof(int) * worker_num);
   displs = (int *)malloc(sizeof(int) * worker_num);

   int offset = 0, step = mul_of_4[MAT_SIZE / (worker_num * 4)] * MAT_SIZE;
   int redundant_ele = MAT_SIZE * MAT_SIZE - step * worker_num;
   int next_step = mul_of_4[1 + MAT_SIZE / (worker_num * 4)] * MAT_SIZE;
   int diff = next_step - step;
   if (redundant_ele > diff)
   {
      int cnt = redundant_ele / diff;
      for (int i = 0; i < cnt; ++i)
      {
         displs[i] = offset;
         sendcounts[i] = next_step;
         offset += next_step;
      }
      for (int i = cnt; i < worker_num; ++i)
      {
         displs[i] = offset;
         sendcounts[i] = step;
         offset += step;
      }
      sendcounts[worker_num - 1] = MAT_SIZE * MAT_SIZE - offset + step;
   }
   else
   {
      for (int i = 0; i < worker_num; ++i)
      {
         displs[i] = offset;
         sendcounts[i] = step;
         offset += step;
      }
      sendcounts[worker_num - 1] = MAT_SIZE * MAT_SIZE - offset + step;
   }

   if (rank == ROOT)
   {
      /* master */
      /* First, fill some numbers into the matrix */
      for (int i = 0, j = 0, k = 0; k < MAT_SIZE * MAT_SIZE;)
      {
         a[k] = i + j;
         b[k] = i * j;
         ++j;
         ++k;
         if (j == MAT_SIZE)
         {
            j = 0;
            ++i;
         }
      }

      /* Measure start time */
      start = MPI_Wtime();
   }

   /* Send matrix data to the worker tasks */
   MPI_Bcast(&a[0], MAT_SIZE * MAT_SIZE, MPI_DOUBLE, ROOT, MPI_COMM_WORLD);                                                   // broadcast matrix a
   MPI_Scatterv(&b[0], sendcounts, displs, MPI_DOUBLE, &b[displs[rank]], sendcounts[rank], MPI_DOUBLE, ROOT, MPI_COMM_WORLD); // scatter columns of matrix b to workers,

   /* worker */
   /* master itself as a worker */
   int st_offset = displs[rank] / MAT_SIZE, ed_offset = (displs[rank] + sendcounts[rank]) / MAT_SIZE;
   register double s, c0, c1, c2, c3;
   double *b0_ptr, *b1_ptr, *b2_ptr, *b3_ptr;
   if (rank == worker_num - 1)
   {
      int rows = ed_offset - st_offset;
      int ed = st_offset + mul_of_4[rows / 4];
      for (int j = st_offset; j < ed; j += 4)
      {
         for (int i = 0; i < MAT_SIZE; ++i)
         {
            c0 = 0.0, c1 = 0.0, c2 = 0.0, c3 = 0.0;
            b0_ptr = &B(0, j), b1_ptr = &B(0, j + 1), b2_ptr = &B(0, j + 2), b3_ptr = &B(0, j + 3);
            for (int k = 0; k < MAT_SIZE; ++k)
            {
               s = A(i, k);
               c0 += s * *b0_ptr;
               c1 += s * *b1_ptr;
               c2 += s * *b2_ptr;
               c3 += s * *b3_ptr;
               b0_ptr++;
               b1_ptr++;
               b2_ptr++;
               b3_ptr++;
            }
            C(i, j) += c0;
            C(i, j + 1) += c1;
            C(i, j + 2) += c2;
            C(i, j + 3) += c3;
         }
      }
      for (int j = ed; j < ed_offset; ++j)
      {
         for (int i = 0; i < MAT_SIZE; ++i)
         {
            c0 = 0.0;
            for (int k = 0; k < MAT_SIZE; ++k)
            {
               c0 += A(i, k) * B(k, j);
            }
            C(i, j) += c0;
         }
      }
   }
   else
   {
      for (int j = st_offset; j < ed_offset; j += 4)
      {
         for (int i = 0; i < MAT_SIZE; ++i)
         {
            c0 = 0.0, c1 = 0.0, c2 = 0.0, c3 = 0.0;
            b0_ptr = &B(0, j), b1_ptr = &B(0, j + 1), b2_ptr = &B(0, j + 2), b3_ptr = &B(0, j + 3);
            for (int k = 0; k < MAT_SIZE; ++k)
            {
               s = A(i, k);
               c0 += s * *b0_ptr;
               c1 += s * *b1_ptr;
               c2 += s * *b2_ptr;
               c3 += s * *b3_ptr;
               b0_ptr++;
               b1_ptr++;
               b2_ptr++;
               b3_ptr++;
            }
            C(i, j) += c0;
            C(i, j + 1) += c1;
            C(i, j + 2) += c2;
            C(i, j + 3) += c3;
         }
      }
   }

   /* Receive results from worker tasks */
   MPI_Gatherv(&c[displs[rank]], sendcounts[rank], MPI_DOUBLE, &c[0], sendcounts, displs, MPI_DOUBLE, ROOT, MPI_COMM_WORLD); // gather the data from workers

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
            if (fabs(C(i, j) - bfRes[i][j]) > EPS)
            {
               flag = 0;
               printf("%d %d %.20lf %.20lf\n", i, j, C(i, j), bfRes[i][j]);
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