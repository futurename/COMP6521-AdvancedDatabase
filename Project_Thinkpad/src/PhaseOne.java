import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

public class PhaseOne {
	
	static String phaseOneFilePrefix = "PhaseOne_";
	static int phaseOneFileNum;
	static int avgCountInPhaseOneFile;
	static int lastCountInPhaseOneFile;
	static int sortedNumCounter = 0;

	static void readAndSortNumbers(String filename, int bfMemSize) throws Exception {
		int numOfInt = bfMemSize / 4 == 0 ? bfMemSize / 4 : bfMemSize / 4 - 1;
		int[] buffer = new int[numOfInt];
		//System.out.println("memsize: " + bfMemSize + " , unit: " + numOfInt);

		FileReader fReader = new FileReader(new File(filename));
		BufferedReader bReader = new BufferedReader(fReader);		
		String[] headers = bReader.readLine().split("\\s+");
		int totalNumbers = Integer.parseInt(headers[0]);
		avgCountInPhaseOneFile = numOfInt;
		lastCountInPhaseOneFile = totalNumbers % numOfInt;		
		MultiwaySortMain.totalNumberValue = totalNumbers;
		System.out.println("init total numbers: " + totalNumbers + " , Memeory restriction: " + headers[1]);		
		bReader.readLine();

		int curPos = 0;
		char curCh;
		String curNumString = "";
		int bufferCount = 0;
		int fileNumCounter = 0;

		while ((curPos = bReader.read()) != -1 ) {
			curCh = (char) curPos;
			if (curCh != '\t') {
				curNumString += curCh;
			} else {				
				buffer[bufferCount++] = Integer.parseInt(curNumString);			
				curNumString = "";
				if ((bufferCount == numOfInt) ) {
					String filePrefix = phaseOneFilePrefix + fileNumCounter;
					fileNumCounter++;
					DataSort.sort(buffer, 0, bufferCount - 1);
					writeBufferToFile(buffer, filePrefix);
					sortedNumCounter += bufferCount;
					//System.out.println("output numbers: " + bufferCount);
					bufferCount = 0;
					
				}				
			}			
		}		
		
		String filePrefix = phaseOneFilePrefix + fileNumCounter;
		fileNumCounter++;
		DataSort.sort(buffer,0,bufferCount-1);
		writeBufferToFile(buffer, filePrefix);		
		sortedNumCounter += bufferCount;		
		System.out.println("total sorted nums in Phase one: " + sortedNumCounter);
		bReader.close();
		phaseOneFileNum = fileNumCounter;
	}

	static void writeBufferToFile(int[] buffer, String filePrefix) throws IOException {
		String fileName = MultiwaySortMain.DEFAULT_TEMP_DATA_DIR + filePrefix + ".txt";
		BufferedWriter bfWriter = new BufferedWriter(new FileWriter(new File(fileName)));
		for (int i : buffer) {
			bfWriter.write(i + "\t");				
		}
		bfWriter.close();
	}

	
	

}
