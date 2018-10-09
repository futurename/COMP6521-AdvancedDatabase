import java.awt.image.DataBuffer;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

import com.sun.org.apache.xerces.internal.xinclude.MultipleScopeNamespaceSupport;

public class PhaseTwo {

	static int dataBufNum = 5;
	static int totalFreeMemUnits = getFreeMemUnits();
	static int outputBufUnits = totalFreeMemUnits / 3;
	static int dataBufUnitSize = (totalFreeMemUnits - outputBufUnits) / dataBufNum;
	static int avgNumInFile = PhaseOne.avgCountInPhaseOneFile;
	static int lastNumInFile = PhaseOne.lastCountInPhaseOneFile;
	static int totalFileNum = PhaseOne.phaseOneFileNum;
	static String fileName = MultiwaySortMain.DEFAULT_TEMP_DATA_DIR + PhaseOne.phaseOneFilePrefix;
	final static String SUFFIX = ".txt";
	static int inputFileCounter = 0;
	static int outputFileCounter = 0;
	static String phaseTwoFilename = MultiwaySortMain.DEFAULT_TEMP_DATA_DIR + "PhaseTwo_";
	static int totalSortedNum = 0;

	static void phaseTwoMain() throws IOException {
		int finalOutputFileNum = totalFileNum;

		while (finalOutputFileNum > 1) {
			int[] numInFileCounterArray = new int[finalOutputFileNum];
			for (int i = 0; i < finalOutputFileNum - 1; i++) {
				numInFileCounterArray[i] = avgNumInFile;
			}
			numInFileCounterArray[finalOutputFileNum - 1] = lastNumInFile;

			int standItrTimes = finalOutputFileNum / dataBufNum;
			int leftFileNum = finalOutputFileNum % dataBufNum;
			// System.out.println("left file" + leftFileNum);
			if (leftFileNum != 0) {
				for (int i = 0; i < standItrTimes; i++) {
					initReadFile(fileName, dataBufNum, dataBufUnitSize, numInFileCounterArray);
					outputFileCounter++;
				}
				
				numInFileCounterArray = new int[leftFileNum];
				for (int i = 0; i < leftFileNum - 1; i++) {
					numInFileCounterArray[i] = avgNumInFile;
				}
				numInFileCounterArray[leftFileNum - 1] = lastNumInFile;

				int specDataBufUnitSize = dataBufUnitSize * dataBufNum / leftFileNum;
				initReadFile(fileName, leftFileNum, specDataBufUnitSize, numInFileCounterArray);
				fileName = phaseTwoFilename;
				phaseTwoFilename += "x_";
				finalOutputFileNum = outputFileCounter + 1;
				inputFileCounter = 0;
				outputFileCounter = 0;
				avgNumInFile = avgNumInFile * dataBufNum;
				lastNumInFile = avgNumInFile * (leftFileNum - 1) + lastNumInFile;
			}
		}
	}

	static void initReadFile(String filePrefix, int dataBufNum, int dataBufUnitSize, int[] numInFileCounterArray)
			throws IOException {
		int[][] dataBuffer = new int[dataBufNum][dataBufUnitSize];

		File[] files = new File[dataBufNum];
		FileReader[] fileFRs = new FileReader[dataBufNum];
		BufferedReader[] fileBRs = new BufferedReader[dataBufNum];

		for (int i = 0; i < dataBufNum; i++) {
			String curFilename = fileName + inputFileCounter + SUFFIX;
			System.out.println("Read from file " + curFilename);
			files[i] = new File(curFilename);
			fileFRs[i] = new FileReader(files[i]);
			fileBRs[i] = new BufferedReader(fileFRs[i]);

			for (int j = 0; j < dataBufUnitSize; j++) {
				dataBuffer[i][j] = readOneNum(fileBRs[i]);
			}

			inputFileCounter++;
		}
		sortAndMerge(fileBRs, dataBufNum, dataBufUnitSize, dataBuffer, numInFileCounterArray);
	}

	static void sortAndMerge(BufferedReader[] fileBRs, int dataBufNum, int dataBufSize, int[][] dataBuffer,
			int[] numInFileCounterArray) throws IOException {
		int[] bufCurPosPointers = new int[dataBufNum];
		int[] outputBuffer = new int[outputBufUnits];
		int outputBufCounter = 0;
		boolean isAllSorted = false;

		while (!isAllSorted) {
			int curMin = getMinNumPos(dataBuffer, bufCurPosPointers, fileBRs, numInFileCounterArray);
			if (curMin != -1) {
				outputBuffer[outputBufCounter++] = curMin;

				if (outputBufCounter == outputBufUnits) {
					String outputFilename = phaseTwoFilename + outputFileCounter;
					totalSortedNum += outputBufCounter;
					System.out.println("totalsortednum written to file: " + totalSortedNum);
					writeToFile(outputFilename, outputBuffer);
					outputBufCounter = 0;
				}
			} else {
				String outputFilename = phaseTwoFilename + outputFileCounter;
				totalSortedNum += outputBufCounter;
				System.out.println("totalsortednum written to file: " + totalSortedNum);
				writeToFile(outputFilename, outputBuffer);
				outputBufCounter = 0;
				isAllSorted = true;
			}
		}
	}

	static void writeToFile(String outputFilename, int[] outputBufdata) throws IOException {
		String curOutputFilename = outputFilename + SUFFIX;
		System.out.println("-------------------------------------------output file: " + curOutputFilename);
		File fw = new File(curOutputFilename);
		FileWriter fWriter = new FileWriter(fw, true);
		BufferedWriter bWriter = new BufferedWriter(fWriter);
		System.out.println("last element : -------->" + outputBufdata[outputBufdata.length - 1]);
		for (int i : outputBufdata) {
			if (i != 0) {
				bWriter.write( i + " " );
			}
		}		 
	}

	static int getMinNumPos(int[][] dataBuffer, int[] bufCurPosPointers, BufferedReader[] fileBRs,
			int[] numInFileCounterArray) throws IOException {

		int[] firstNums = new int[bufCurPosPointers.length];

		for (int i = 0; i < dataBuffer.length; i++) {
			firstNums[i] = dataBuffer[i][bufCurPosPointers[i]];
		}

		int min = firstNums[0], minSeq = 0;
		for (int i = 1; i < dataBuffer.length; i++) {
			if (min != -1) {
				if (firstNums[i] < min && firstNums[i] != -1) {
					min = firstNums[i];
					minSeq = i;
				}
			} else {
				min = firstNums[i];
				minSeq = i;
			}
		}

		if (min != -1) {
			bufCurPosPointers[minSeq]++;
			if (bufCurPosPointers[minSeq] == numInFileCounterArray[minSeq]) {
				for (int i = 0; i < dataBuffer.length; i++) {
					System.out
							.println("*****file: " + i + " , reach file end, numinbuffer: " + numInFileCounterArray[i]);
				}
				bufCurPosPointers[minSeq]--;
				dataBuffer[minSeq][bufCurPosPointers[minSeq]] = -1;
			} else if (bufCurPosPointers[minSeq] == dataBuffer[0].length && numInFileCounterArray[minSeq] > 0) {
				System.out.println(dataBuffer[0].length);
				int leftNum = numInFileCounterArray[minSeq] - dataBuffer[0].length;
				int readNums = dataBuffer[0].length < leftNum ? dataBuffer[0].length : leftNum;
				System.out.println("file: " + minSeq + " ,left: " + leftNum);
				numInFileCounterArray[minSeq] = leftNum;
				if (readNums > 0) {
					for (int i = 0; i < readNums; i++) {
						dataBuffer[minSeq][i] = readOneNum(fileBRs[minSeq]);
						bufCurPosPointers[minSeq] = 0;
					}
				}
			}
		}
		//System.out.println("Current seq: " + minSeq + ",min: " + min);
		return min;
	}

	static int calItrTimes(int totalFileNum, int dataBufNum) {
		int result = 0;
		if (totalFileNum <= dataBufNum) {
			result = 1;
		} else {
			result = totalFileNum % dataBufNum == 0 ? totalFileNum / dataBufNum : (totalFileNum / dataBufNum + 1);
		}
		return result + calItrTimes(result, dataBufNum);
	}

	static int getFreeMemUnits() {
		long freeMem = MultiwaySortMain.getFreeMemSize();
		long freeMemUnits = freeMem / 4;
		return (int) freeMemUnits;
	}

	static int readOneNum(BufferedReader br) throws IOException {
		int curPos = 0;
		char curCh;
		String curNumStr = "";
		while ((char) (curPos = br.read()) != ' ') {
			curCh = (char) curPos;
			curNumStr += curCh;
		}
		return Integer.parseInt(curNumStr);
	}

}
