import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

import com.sun.swing.internal.plaf.metal.resources.metal_zh_TW;

import jdk.internal.dynalink.beans.StaticClass;

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

			int standItrTimes = finalOutputFileNum / dataBufNum;
			System.out.println("standard itr time: " + standItrTimes);
			int leftFileNum = finalOutputFileNum % dataBufNum;
			System.out.println("left file num: " + leftFileNum);

			for (int i = 0; i < standItrTimes; i++) {
				int[] numInFileCounterArray = new int[dataBufNum];
				for (int j = 0; j < dataBufNum - 1; j++) {
					numInFileCounterArray[i] = avgNumInFile;
				}
				int[] sortedNumFromFileCounterArray = new int[dataBufNum];
				System.out.println("total num need be sorted: " + (dataBufNum * avgNumInFile));
				initReadFile(fileName, dataBufNum, dataBufUnitSize, numInFileCounterArray,
						sortedNumFromFileCounterArray);
				outputFileCounter++;
			}
			if (leftFileNum != 0) {
				int[] numInFileCounterArray = new int[leftFileNum];
				for (int i = 0; i < leftFileNum - 1; i++) {
					numInFileCounterArray[i] = avgNumInFile;
				}
				numInFileCounterArray[leftFileNum - 1] = lastNumInFile;
				int[] sortedNumFromFileCounterArray = new int[leftFileNum];
				int specDataBufUnitSize = dataBufUnitSize * dataBufNum / leftFileNum;
				initReadFile(fileName, leftFileNum, specDataBufUnitSize, numInFileCounterArray,
						sortedNumFromFileCounterArray);
				outputFileCounter++;
			}
			fileName = phaseTwoFilename;
			phaseTwoFilename += "x_";
			finalOutputFileNum = outputFileCounter;
			inputFileCounter = 0;
			outputFileCounter = 0;
			avgNumInFile = avgNumInFile * dataBufNum;
			lastNumInFile = avgNumInFile * (leftFileNum - 1) + lastNumInFile;
		}
	}

	static void initReadFile(String filePrefix, int dataBufNum, int dataBufUnitSize, int[] numInFileCounterArray,
			int[] sortedNumCounterArray) throws IOException {
		int[][] dataBuffer = new int[dataBufNum][dataBufUnitSize];

		File[] files = new File[dataBufNum];
		FileReader[] fileFRs = new FileReader[dataBufNum];
		BufferedReader[] fileBRs = new BufferedReader[dataBufNum];

		for (int i = 0; i < dataBufNum; i++) {
			String curFilename = fileName + inputFileCounter + SUFFIX;
			files[i] = new File(curFilename);
			fileFRs[i] = new FileReader(files[i]);
			fileBRs[i] = new BufferedReader(fileFRs[i]);
			System.out.println("Read from file " + curFilename);
			for (int j = 0; j < dataBufUnitSize; j++) {
				dataBuffer[i][j] = readOneNum(fileBRs[i]);
				// System.out.println("reading i: " + i + " , j:" + j);
			}
			inputFileCounter++;
		}
		sortAndMerge(fileBRs, dataBufNum, dataBufUnitSize, dataBuffer, numInFileCounterArray, sortedNumCounterArray);

	}

	static void sortAndMerge(BufferedReader[] fileBRs, int dataBufNum, int dataBufSize, int[][] dataBuffer,
			int[] numInFileCounterArray, int[] sortedNumCounter) throws IOException {
		int[] bufCurPosPointers = new int[dataBufNum];
		int[] outputBuffer = new int[outputBufUnits];
		int outputBufCounter = 0;
		boolean isAllSorted = false;

		while (!isAllSorted) {
			int curMin = getMinNumPos(dataBuffer, bufCurPosPointers, fileBRs, numInFileCounterArray, sortedNumCounter);
			if (curMin != -1) {
				outputBuffer[outputBufCounter++] = curMin;
				if (outputBufCounter == outputBufUnits) {
					String outputFilename = phaseTwoFilename + outputFileCounter;
					totalSortedNum += outputBufCounter;
					System.out.println("totalsortednum written to file(full): " + totalSortedNum);
					writeToFile(outputFilename, outputBuffer);
					outputBufCounter = 0;
				}
			} else {
				String outputFilename = phaseTwoFilename + outputFileCounter;
				totalSortedNum += outputBufCounter;
				System.out.println("totalsortednum written to file(unfull): " + totalSortedNum);
				writeToFile(outputFilename, outputBuffer);
				outputBufCounter = 0;
				isAllSorted = true;
			}
		}
	}

	static void writeToFile(String outputFilename, int[] outputBufdata) throws IOException {
		String curOutputFilename = outputFilename + SUFFIX;
		System.out.println("-----------------output file: " + curOutputFilename);
		File fw = new File(curOutputFilename);
		FileWriter fWriter = new FileWriter(fw, true);
		BufferedWriter bWriter = new BufferedWriter(fWriter);
		// System.out.println("last element : -------->" +
		// outputBufdata[outputBufdata.length - 1]);
		for (int i : outputBufdata) {
			bWriter.write(i + "\t");
		}
	}

	static int getMinNumPos(int[][] dataBuffer, int[] bufCurPosPointers, BufferedReader[] fileBRs,
			int[] numInFileCounterArray, int[] sortedNumFromFileCounter) throws IOException {
		int dataUnitSize = dataBuffer.length;
		int[] firstNums = new int[dataUnitSize];

		for (int i = 0; i < dataUnitSize; i++) {
			firstNums[i] = dataBuffer[i][bufCurPosPointers[i]];
		}

		int min = firstNums[0], minSeq = 0;
		for (int i = 1; i < dataUnitSize; i++) {
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
			//sortedNumFromFileCounter[minSeq]++;
			//System.out.println("minseq:" + minSeq + ",sortednum:" + sortedNumFromFileCounter[minSeq]);
			if (sortedNumFromFileCounter[minSeq] == numInFileCounterArray[minSeq]) {
				bufCurPosPointers[minSeq]--;
				dataBuffer[minSeq][bufCurPosPointers[minSeq]] = -1;
				System.out.println("channel: " + minSeq + " finished, pointer pos: " + bufCurPosPointers[minSeq]);
			}
			if (bufCurPosPointers[minSeq] == dataUnitSize) {
				int unsortedNum = numInFileCounterArray[minSeq] - sortedNumFromFileCounter[minSeq];
				if (unsortedNum >= dataUnitSize) {
					for (int i = 0; i < dataUnitSize; i++) {
						System.out.println("buffer size: " + dataUnitSize + " ,Channel: " + minSeq + ". unsorted num: "
								+ unsortedNum + " ,totalnum: " + numInFileCounterArray[minSeq]);
						dataBuffer[minSeq][i] = readOneNum(fileBRs[minSeq]);
					}
					bufCurPosPointers[minSeq] = 0;
				} else {
					for (int i = 0; i < unsortedNum; i++) {
						System.out.println("buffer size: " + dataUnitSize + " ,Channel: " + minSeq + ". unsorted num: "
								+ unsortedNum + " ,totalnum: " + numInFileCounterArray[minSeq]);
						dataBuffer[minSeq][i] = readOneNum(fileBRs[minSeq]);						
					}
					bufCurPosPointers[minSeq] = 0;
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
		while ((char) (curPos = br.read()) != '\t') {
			curCh = (char) curPos;

			curNumStr += curCh;
		}
		// System.out.println(curNumStr);
		return Integer.parseInt(curNumStr);
	}

}
