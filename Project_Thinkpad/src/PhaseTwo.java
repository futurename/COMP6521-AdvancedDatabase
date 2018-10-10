import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

import javax.swing.plaf.basic.BasicInternalFrameTitlePane.SystemMenuBar;

import com.sun.jmx.snmp.SnmpUnknownSubSystemException;
import com.sun.swing.internal.plaf.metal.resources.metal_zh_TW;

import jdk.internal.dynalink.beans.StaticClass;
import sun.security.x509.AVA;

public class PhaseTwo {
	static int dataBufNum = 5;
	static int totalFreeMemUnits = getFreeMemUnits();
	static int outputBufUnits = totalFreeMemUnits / 2;
	static int dataBufUnitSize = (totalFreeMemUnits - outputBufUnits) / dataBufNum;
	static int avgNumInFile = PhaseOne.avgCountInPhaseOneFile;
	static int lastNumInFile = PhaseOne.lastCountInPhaseOneFile;
	static int totalFileNum = PhaseOne.phaseOneFileNum;
	static String fileName = MultiwaySortMain.DEFAULT_TEMP_DATA_DIR + PhaseOne.phaseOneFilePrefix;
	final static String SUFFIX = ".txt";
	static int[] numInFile;	
	static int totalSortedNumCounter = 0;
	static int[] sortedNumInFile;
	static int inputFileCounter = 0;
	static int outputFileCounter = 0;
	static String phaseTwoFilename = MultiwaySortMain.DEFAULT_TEMP_DATA_DIR + "PhaseTwo_";
	static int totalSortedNum = 0;

	static void phaseTwoMain() throws IOException {
		int finalOutputFileNum = totalFileNum;
		System.out.println("Freemenunits: " + totalFreeMemUnits);

		while (finalOutputFileNum > 1) {

			int standItrTimes = finalOutputFileNum / dataBufNum;
			System.out.println("databuf, current round: " + dataBufNum);
			System.out.println("standard itr time: " + standItrTimes);
			int leftFileNum = finalOutputFileNum % dataBufNum;
			System.out.println("left file num: " + leftFileNum);
			//System.out.println("----------filenameprefix in this round: " + fileName);
			
			for (int i = 0; i < standItrTimes; i++) {
				numInFile = new int[dataBufNum];
				for (int j = 0; j < dataBufNum; j++) {
					numInFile[j] = avgNumInFile;
					System.out.println("numinfile: " + numInFile[j]);
				}
				sortedNumInFile = new int[dataBufNum];
				//System.out.println("total num need be sorted: " + (dataBufNum * avgNumInFile));
				initReadFile(fileName, dataBufNum, dataBufUnitSize, numInFile);
				outputFileCounter++;
			}

			if (leftFileNum != 0) {
				numInFile = new int[leftFileNum];
				for (int i = 0; i < leftFileNum - 1; i++) {					
					numInFile[i] = avgNumInFile;
					
					//System.out.println("numinfile: " + numInFile[i]);
				}
				numInFile[leftFileNum - 1] = lastNumInFile;
				System.out.println("avgnuminfile: " + avgNumInFile + " ,numinlastfile: " + lastNumInFile);
				//System.out.println("numinfile: " + numInFile[leftFileNum- 1]);
				sortedNumInFile = new int[leftFileNum];
				int specDataBufUnitSize = dataBufUnitSize * dataBufNum / leftFileNum;
				initReadFile(fileName, leftFileNum, specDataBufUnitSize, numInFile);
				outputFileCounter++;
			}
			System.out.println("totalsortednum: " + totalSortedNum);
			totalSortedNum = 0;
			fileName = phaseTwoFilename;
			phaseTwoFilename += "x_";
			finalOutputFileNum = outputFileCounter;
			inputFileCounter = 0;
			outputFileCounter = 0;
			avgNumInFile = avgNumInFile * dataBufNum;
			lastNumInFile = avgNumInFile * (leftFileNum - 1) + lastNumInFile;			
			totalSortedNum = 0;
			int newDataBufNum = finalOutputFileNum < dataBufNum ? finalOutputFileNum : dataBufNum;
			resizeBuffer(newDataBufNum);
			dataBufNum = newDataBufNum;
		}
	}
	
	static void resizeBuffer(int dataBufNum) {
		System.gc();
		long freemem = Runtime.getRuntime().freeMemory();
		int freeunits = (int) (freemem / 4 * MultiwaySortMain.DEFAULT_MEM_RATE);
		
		System.out.println("freememunits: " + freeunits);
		outputBufUnits = freeunits / 2;
		dataBufUnitSize = (freeunits - outputBufUnits) / dataBufNum;
	}

	static void initReadFile(String filePrefix, int dataBufNum, int dataBufUnitSize, int[] numInThisFile) throws IOException {
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
		sortAndMerge(fileBRs, dataBufNum, dataBufUnitSize, dataBuffer, numInThisFile);

	}

	static void sortAndMerge(BufferedReader[] fileBRs, int dataBufNum, int dataBufSize, int[][] dataBuffer, int[] numInThisFile)
			throws IOException {
		int[] bufCurPosPointers = new int[dataBufNum];
		int[] outputBuffer = new int[outputBufUnits];
		int outputBufCounter = 0;
		boolean isAllSorted = false;

		while (!isAllSorted) {
			int curMin = getMinNumPos(dataBuffer, bufCurPosPointers, fileBRs, numInThisFile);
			if (curMin != -1) {
				outputBuffer[outputBufCounter++] = curMin;
			
				if (outputBufCounter == outputBufUnits) {
					String outputFilename = phaseTwoFilename + outputFileCounter;
					totalSortedNum += outputBufCounter;
					//System.out.println("totalsortednum written to file(full): " + totalSortedNum);
					writeToFile(outputFilename, outputBuffer);
					outputBufCounter = 0;
				}
			} else {
				String outputFilename = phaseTwoFilename + outputFileCounter;
				totalSortedNum += outputBufCounter;
				//System.out.println("totalsortednum written to file(unfull): " + totalSortedNum);
				writeToFile(outputFilename, outputBuffer);
				outputBufCounter = 0;
				isAllSorted = true;
			}
		}
	}

	static void writeToFile(String outputFilename, int[] outputBufdata) throws IOException {
		String curOutputFilename = outputFilename + SUFFIX;
		//System.out.println("-----------------output file: " + curOutputFilename);
		File fw = new File(curOutputFilename);
		FileWriter fWriter = new FileWriter(fw, true);
		BufferedWriter bWriter = new BufferedWriter(fWriter);
		// System.out.println("last element : -------->" +
		// outputBufdata[outputBufdata.length - 1]);
		for (int i : outputBufdata) {
			bWriter.write(i + "\t");
		}
		bWriter.close();
	}

	static int getMinNumPos(int[][] dataBuffer, int[] bufCurPosPointers, BufferedReader[] fileBRs, int[] numInThisFile) throws IOException {
		int dataUnitSize = dataBuffer.length;
		int[] firstNums = new int[dataUnitSize];

		for (int i = 0; i < dataUnitSize; i++) {
			firstNums[i] = dataBuffer[i][bufCurPosPointers[i]];
			// System.out.println("channel: " + i + ",curpos: " + bufCurPosPointers[i] + "
			// num: " + firstNums[i]);
		}

		int min = firstNums[0], minSeq = 0;
		for (int i = 1; i < dataUnitSize; i++) {
			if (min != -1) {
				if (firstNums[i] < min && firstNums[i] != -1) {
					min = firstNums[i];
					minSeq = i;
					// System.out.println("firstNum[" + i + "]:" + firstNums[i] + ",min:" + min +
					// ",minseq:" + minSeq);
				}
			} else {
				min = firstNums[i];
				minSeq = i;
			}
		}

		if (min != -1) {
			bufCurPosPointers[minSeq]++;
			// System.out.println("channel: " + minSeq + ",curpos will ++ to: " +
			// bufCurPosPointers[minSeq]);
			//System.out.println("sortednum: " + sortedNumInFile[minSeq] + ",numInfile: " + numInThisFile[minSeq]);
			sortedNumInFile[minSeq]++;
			// System.out.println("minseq:" + minSeq + ",sortednum:" +
			// sortedNumFromFileCounter[minSeq]);
			if (sortedNumInFile[minSeq] == numInThisFile[minSeq]) {
				bufCurPosPointers[minSeq]--;
				dataBuffer[minSeq][bufCurPosPointers[minSeq]] = -1;
				// System.out.println("channel: " + minSeq + " finished, pointer pos: " +
				// bufCurPosPointers[minSeq]);
			}
			if (bufCurPosPointers[minSeq] == dataBuffer[0].length) {
				int unsortedNum = numInThisFile[minSeq] - sortedNumInFile[minSeq];
				if (unsortedNum >= dataUnitSize) {
					for (int i = 0; i < dataUnitSize; i++) {						
						dataBuffer[minSeq][i] = readOneNum(fileBRs[minSeq]);
					}
					bufCurPosPointers[minSeq] = 0;
				} else {
					for (int i = 0; i < unsortedNum; i++) {
						/*
						 * System.out.println("buffer size: " + dataUnitSize + " ,Channel: " + minSeq +
						 * ". unsorted num: " + unsortedNum + " ,totalnum: " + numInFile);
						 */
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
