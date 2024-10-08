#include <stdio.h>
#include <iostream>
#include <iomanip>
#include <time.h>
#include <cstdlib>
#include <papi.h>
#include <fstream>
#include <omp.h>

using namespace std;

#define SYSTEMTIME clock_t

double OnMult(int m_ar, int m_br) {
	SYSTEMTIME Time1, Time2;
	
	char st[100];
	double temp;
	int i, j, k;

	double *pha, *phb, *phc;
		
    pha = (double *)malloc((m_ar * m_ar) * sizeof(double));
	phb = (double *)malloc((m_ar * m_ar) * sizeof(double));
	phc = (double *)malloc((m_ar * m_ar) * sizeof(double));

	for(i=0; i<m_ar; i++)
		for(j=0; j<m_ar; j++)
			pha[i*m_ar + j] = (double)1.0;

	for(i=0; i<m_br; i++)
		for(j=0; j<m_br; j++)
			phb[i*m_br + j] = (double)(i+1);

    Time1 = clock();

	for(i=0; i<m_ar; i++)
	{	for( j=0; j<m_br; j++)
		{	temp = 0;
			for( k=0; k<m_ar; k++)
			{	
				temp += pha[i*m_ar+k] * phb[k*m_br+j];
			}
			phc[i*m_ar+j]=temp;
		}
	}


    Time2 = clock();
	sprintf(st, "Time: %3.3f seconds\n", (double)(Time2 - Time1) / CLOCKS_PER_SEC);
	cout << st;

	cout << "Result matrix: " << endl;
	for(i=0; i<1; i++)
	{	for(j=0; j<min(10,m_br); j++)
			cout << phc[j] << " ";
	}
	cout << endl;

    free(pha);
    free(phb);
    free(phc);	

	return (double)(Time2 - Time1) / CLOCKS_PER_SEC;
}

double OnMultLine(int m_ar, int m_br){
	SYSTEMTIME Time1, Time2;
	
	char st[100];
	double temp;
	int i, j, k;

	double *pha, *phb, *phc;
		
    pha = (double *)malloc((m_ar * m_ar) * sizeof(double));
	phb = (double *)malloc((m_ar * m_ar) * sizeof(double));
	phc = (double *)malloc((m_ar * m_ar) * sizeof(double));

	for(i=0; i<m_ar; i++)
		for(j=0; j<m_ar; j++)
			pha[i*m_ar + j] = (double)1.0;

	for(i=0; i<m_br; i++)
		for(j=0; j<m_br; j++)
			phb[i*m_br + j] = (double)(i+1);

    Time1 = clock();

	for(i=0; i<m_ar; i++)
	{	
		for( k=0; k<m_ar; k++)
		{	
			for( j=0; j<m_br; j++)
			{	
				phc[i*m_ar+j] += pha[i*m_ar+k] * phb[k*m_br+j];
			}
		}
	}

    Time2 = clock();
	sprintf(st, "Time: %3.3f seconds\n", (double)(Time2 - Time1) / CLOCKS_PER_SEC);
	cout << st;

	cout << "Result matrix: " << endl;
	for(i=0; i<1; i++)
	{	for(j=0; j<min(10,m_br); j++)
			cout << phc[j] << " ";
	}
	cout << endl;

    free(pha);
    free(phb);
    free(phc);	 

	return (double)(Time2 - Time1) / CLOCKS_PER_SEC;   
}

double OnMultBlock(int m_ar, int m_br, int bkSize)
{
	clock_t Time1, Time2;
	
	char st[100];
	double temp;
	int i, j, k, blockRowStart, blockColStart;

	double *pha, *phb, *phc;
		
	pha = (double *)malloc((m_ar * m_ar) * sizeof(double));
	phb = (double *)malloc((m_ar * m_ar) * sizeof(double));
	phc = (double *)malloc((m_ar * m_ar) * sizeof(double));

	for(i=0; i<m_ar; i++)
		for(j=0; j<m_ar; j++)
			pha[i*m_ar + j] = (double)1.0;

	for(i=0; i<m_br; i++)
		for(j=0; j<m_br; j++)
			phb[i*m_br + j] = (double)(i+1);

	for(i=0; i<m_ar; i++)
		for(j=0; j<m_ar; j++)
			phc[i*m_ar + j] = 0.0;

	Time1 = clock();

	for (int rowBlockStart = 0; rowBlockStart < m_ar; rowBlockStart += bkSize){
		for (int colBlockStart = 0; colBlockStart < m_ar; colBlockStart += bkSize){
			for (int innerBlockStart = 0; innerBlockStart < m_ar; innerBlockStart += bkSize){
				for (int row = rowBlockStart; row < rowBlockStart + bkSize; row++){
					for (int col = colBlockStart; col < colBlockStart + bkSize; col++){
						for (int inner = innerBlockStart; inner < innerBlockStart + bkSize; inner++){
							phc[row*m_ar + inner] += pha[row*m_ar + col] * phb[col*m_br + inner];
						}
					}
				}
			}
		}
	}

	Time2 = clock();
	sprintf(st, "Time: %3.3f seconds\n", (double)(Time2 - Time1) / CLOCKS_PER_SEC);
	cout << st;

	cout << "Result matrix: " << endl;
	for(i=0; i<1; i++)
	{   for(j=0; j<min(10,m_br); j++)
			cout << phc[j] << " ";
	}

	cout << endl;

	free(pha);
	free(phb);
	free(phc);
	
	return (double)(Time2 - Time1) / CLOCKS_PER_SEC;
}

double OnMultLineParallel_1(int m_ar, int m_br){
    double temp;
    int i, j, k;

    double *pha, *phb, *phc;
        
    pha = (double *)malloc((m_ar * m_ar) * sizeof(double));
    phb = (double *)malloc((m_ar * m_ar) * sizeof(double));
    phc = (double *)malloc((m_ar * m_ar) * sizeof(double));

    for(i=0; i<m_ar; i++)
        for(j=0; j<m_ar; j++)
            pha[i*m_ar + j] = (double)1.0;

    for(i=0; i<m_br; i++)
        for(j=0; j<m_br; j++)
            phb[i*m_br + j] = (double)(i+1);

    double Time1 = omp_get_wtime();

	int n_threads = omp_get_max_threads();
    std::cout << "Number of threads: " << n_threads << std::endl;

    #pragma omp parallel for num_threads(n_threads) private(j, k)
    for(i=0; i<m_ar; i++){   
        for( k=0; k<m_ar; k++){   
            for( j=0; j<m_br; j++){   
                phc[i*m_ar+j] += pha[i*m_ar+k] * phb[k*m_br+j];
            }
        }
    }

    double Time2 = omp_get_wtime();
    printf("Time: %3.3f seconds\n", Time2 - Time1);

    std::cout << "Result matrix: " << std::endl;
    for(i=0; i<1; i++)
    {   for(j=0; j<std::min(10,m_br); j++)
            std::cout << phc[j] << " ";
    }
    std::cout << std::endl;

    free(pha);
    free(phb);
    free(phc);     

    return Time2 - Time1;   
}

double OnMultLineParallel_2(int m_ar, int m_br){
	double temp;
	int i, j, k;

	double *pha, *phb, *phc;
		
	pha = (double *)malloc((m_ar * m_ar) * sizeof(double));
	phb = (double *)malloc((m_ar * m_ar) * sizeof(double));
	phc = (double *)malloc((m_ar * m_ar) * sizeof(double));

	for(i=0; i<m_ar; i++)
		for(j=0; j<m_ar; j++)
			pha[i*m_ar + j] = (double)1.0;

	for(i=0; i<m_br; i++)
		for(j=0; j<m_br; j++)
			phb[i*m_br + j] = (double)(i+1);

	double Time1 = omp_get_wtime();

	int n_threads = omp_get_max_threads();
    std::cout << "Number of threads: " << n_threads << std::endl;

	#pragma omp parallel private(i, j, k) num_threads(n_threads)
    for(i=0; i<m_ar; i++){   
        for( k=0; k<m_ar; k++){   
            #pragma omp for 
            for( j=0; j<m_br; j++){   
                {
                    phc[i*m_ar+j] += pha[i*m_ar+k] * phb[k*m_br+j];
                }
            }
        }
    }

	double Time2 = omp_get_wtime();
	printf("Time: %3.3f seconds\n", Time2 - Time1);

	std::cout << "Result matrix: " << std::endl;
	for(i=0; i<1; i++)
	{   for(j=0; j<std::min(10,m_br); j++)
			std::cout << phc[j] << " ";
	}
	std::cout << std::endl;

	free(pha);
	free(phb);
	free(phc);     

	return Time2 - Time1;   
}

void handle_error (int retval)
{
  printf("PAPI error %d: %s\n", retval, PAPI_strerror(retval));
  exit(1);
}

void init_papi() {
  int retval = PAPI_library_init(PAPI_VER_CURRENT);
  if (retval != PAPI_VER_CURRENT && retval < 0) {
    printf("PAPI library version mismatch!\n");
    exit(1);
  }
  if (retval < 0) handle_error(retval);

  std::cout << "PAPI Version Number: MAJOR: " << PAPI_VERSION_MAJOR(retval)
            << " MINOR: " << PAPI_VERSION_MINOR(retval)
            << " REVISION: " << PAPI_VERSION_REVISION(retval) << "\n";
}


int main (int argc, char *argv[])
{

	char c;
	int lin, col, blockSize;
	int op;
	
	int EventSet = PAPI_NULL;
  	long long values[2];
  	int ret;
	

	ret = PAPI_library_init( PAPI_VER_CURRENT );
	if ( ret != PAPI_VER_CURRENT )
		std::cout << "FAIL" << endl;


	ret = PAPI_create_eventset(&EventSet);
		if (ret != PAPI_OK) cout << "ERROR: create eventset" << endl;


	ret = PAPI_add_event(EventSet,PAPI_L1_DCM );
	if (ret != PAPI_OK) cout << "ERROR: PAPI_L1_DCM" << endl;


	ret = PAPI_add_event(EventSet,PAPI_L2_DCM);
	if (ret != PAPI_OK) cout << "ERROR: PAPI_L2_DCM" << endl;

	ofstream outputFile;

	/*
	outputFile.open("resultsMultCpp.csv");
	if (!outputFile.is_open()) {
		cout << "Error: Unable to open the file for writing." << endl;
		return 1;
	}

	outputFile << "Try,Dimension,Time,L1_DCM,L2_DCM" << endl;

	for (int trial = 0; trial < 10; trial++) {
		for (int dim = 600; dim <= 3000; dim += 400) {
				cout << "Trial:" << trial << endl;
				cout << "Dim:" << dim << endl;

				ret = PAPI_start(EventSet);
				if (ret != PAPI_OK) cout << "ERROR: Start PAPI" << endl;

				double elapsed_time = OnMult(dim, dim);

				ret = PAPI_stop(EventSet, values);
				if (ret != PAPI_OK) cout << "ERROR: Stop PAPI" << endl;

				outputFile << trial << "," << dim << "," << elapsed_time << "," << values[0] << "," << values[1] << endl;

				ret = PAPI_reset(EventSet);
				if (ret != PAPI_OK)
					std::cout << "FAIL reset" << endl;

				cout << endl
					<< endl;
		}
	}

	//From 600 to 3000, step 400 (OnMultLine)
	outputFile.open("resultsMultLineCpp.csv");
	if (!outputFile.is_open()) {
		cout << "Error: Unable to open the file for writing." << endl;
		return 1;
	}

	outputFile << "Try,Dimension,Time,L1_DCM,L2_DCM" << endl;

	//From 4096 to 10240, step 2048 (OnMultLine)
	for (int trial = 0; trial < 10; trial++) {
		for (int dim = 600; dim <= 3000; dim += 400) {
				cout << "Trial:" << trial << endl;
				cout << "Dim:" << dim << endl;

				ret = PAPI_start(EventSet);
				if (ret != PAPI_OK) cout << "ERROR: Start PAPI" << endl;

				double elapsed_time = OnMultLine(dim, dim);

				ret = PAPI_stop(EventSet, values);
				if (ret != PAPI_OK) cout << "ERROR: Stop PAPI" << endl;

				outputFile << trial << "," << dim << "," << elapsed_time << "," << values[0] << "," << values[1] << endl;

				ret = PAPI_reset(EventSet);
				if (ret != PAPI_OK)
					std::cout << "FAIL reset" << endl;

				cout << endl
					<< endl;
		}
	}

	for (int trial = 0; trial < 10; trial++) {
		for (int dim = 4096; dim <= 10240; dim += 2048) {
				cout << "Trial:" << trial << endl;
				cout << "Dim:" << dim << endl;

				ret = PAPI_start(EventSet);
				if (ret != PAPI_OK) cout << "ERROR: Start PAPI" << endl;

				double elapsed_time = OnMultLine(dim, dim);

				ret = PAPI_stop(EventSet, values);
				if (ret != PAPI_OK) cout << "ERROR: Stop PAPI" << endl;

				outputFile << trial << "," << dim << "," << elapsed_time << "," << values[0] << "," << values[1] << endl;

				ret = PAPI_reset(EventSet);
				if (ret != PAPI_OK)
					std::cout << "FAIL reset" << endl;

				cout << endl
					<< endl;
		}
	}

	outputFile.close();

	//From 4096 to 10240 with intervals of 2048 for block sizes (128,256,512) (OnMultBlock)
	outputFile.open("resultsMultBlockCpp.csv");
	if (!outputFile.is_open()) {
		cout << "Error: Unable to open the file for writing." << endl;
		return 1;
	}

	outputFile << "Try, Dimension,BlockSize,Time,L1_DCM,L2_DCM" << endl;
	for (int trial = 0; trial < 10; trial++){
		for (int dim = 4096; dim <= 10240; dim += 2048) {
			for (int blkSize = 128; blkSize <= 512; blkSize *= 2) {
				cout << "Trial:" << trial << endl;
				cout << "Dim:" << dim << endl;
				cout << "BlockSize:" << blkSize << endl;

				ret = PAPI_start(EventSet);
				if (ret != PAPI_OK) cout << "ERROR: Start PAPI" << endl;

				double elapsed_time = OnMultBlock(dim, dim, blkSize);

				ret = PAPI_stop(EventSet, values);
				if (ret != PAPI_OK) cout << "ERROR: Stop PAPI" << endl;

				outputFile << trial << "," << dim << "," << blkSize << "," << elapsed_time << "," << values[0] << "," << values[1] << endl;

				ret = PAPI_reset(EventSet);
				if (ret != PAPI_OK)
					std::cout << "FAIL reset" << endl;

				cout << endl
					<< endl;
			}
		}
	}
	outputFile.close();



	outputFile.open("resultsMultLineParallel_1.csv");
	if (!outputFile.is_open()) {
		cout << "Error: Unable to open the file for writing." << endl;
		return 1;
	}

	outputFile << "Try,Dimension,Time,L1_DCM,L2_DCM,FLOPS" << endl;
	for (int trial = 0; trial < 10; trial++){
		for (int dim = 4096; dim <= 10240; dim += 2048) {
			cout << "Trial:" << trial << endl;
			cout << "Dim:" << dim << endl;

			ret = PAPI_start(EventSet);
			if (ret != PAPI_OK) cout << "ERROR: Start PAPI" << endl;

			double elapsed_time = OnMultLineParallel_1(dim, dim);

			ret = PAPI_stop(EventSet, values);
			if (ret != PAPI_OK) cout << "ERROR: Stop PAPI" << endl;

        	double flops = (2.0 * (dim^3)) / elapsed_time;

			outputFile << trial << "," << dim << "," << elapsed_time << "," << values[0] << "," << values[1] << "," << flops << endl;

			cout << endl
				<< endl;
		}
	}
	outputFile.close();
*/
	outputFile.open("resultsMultLineParallel_2.csv");
	if (!outputFile.is_open()) {
		cout << "Error: Unable to open the file for writing." << endl;
		return 1;
	}

	outputFile << "Try,Dimension,Time,L1_DCM,L2_DCM,FLOPS" << endl;
	for (int trial = 0; trial < 10; trial++){
		for (int dim = 4096; dim <= 10240; dim += 2048) {
			cout << "Trial:" << trial << endl;
			cout << "Dim:" << dim << endl;

			ret = PAPI_start(EventSet);
			if (ret != PAPI_OK) cout << "ERROR: Start PAPI" << endl;

			double elapsed_time = OnMultLineParallel_2(dim, dim);

			ret = PAPI_stop(EventSet, values);
			if (ret != PAPI_OK) cout << "ERROR: Stop PAPI" << endl;

			double flops = (2.0 * (dim^3)) / elapsed_time;

			outputFile << trial << "," << dim << "," << elapsed_time << "," << values[0] << "," << values[1] << "," << flops << endl;

			cout << endl << endl;
		}
	}

	outputFile.close();

	ret = PAPI_remove_event( EventSet, PAPI_L1_DCM );
	if ( ret != PAPI_OK )
		std::cout << "FAIL remove event" << endl; 

	ret = PAPI_remove_event( EventSet, PAPI_L2_DCM );
	if ( ret != PAPI_OK )
		std::cout << "FAIL remove event" << endl; 

	ret = PAPI_destroy_eventset( &EventSet );
	if ( ret != PAPI_OK )
		std::cout << "FAIL destroy" << endl;
}