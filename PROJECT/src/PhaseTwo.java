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
import jdk.internal.org.objectweb.asm.tree.IntInsnNode;
import sun.security.x509.AVA;

public class PhaseTwo {
	static final int DEFAULT_DATA_BUF_NUM = PhaseOne.phaseOneFileNum < 7 ? PhaseOne.phaseOneFileNum : 7;
	static int dataBufNum = DEFAULT_DATA_BUF_NUM;
	static int totalFreeMemUnits = getFreeMemUnits();
	static int outputBufUnits = totalFreeMemUnits / 2;
	static int dataBufUnitSize = (totalFreeMemUnits - outputBufUnits) / dataBufNum;
	static int avgNumInFile = PhaseOne.avgCountInPhaseOneFile;
	static int lastNumInFile = PhaseOne.lastCountInPhaseOneFile;
	static int totalFileNum = PhaseOne.phaseOneFileNum;
	static int fileNumConter = totalFileNum;
	static String fileName = MultiwaySortMain.DEFAULT_TEMP_DATA_DIR + PhaseOne.phaseOneFilePrefix;
	final static String SUFFIX = ".txt";
	static int[] numInFile;
	static int bufUpperLimit;
	static int totalSortedNumCounter = 0;
	static int[] sortedNumInFile;
	static int inputFileCounter = 0;
	static int outputFileCounter = 0;
	static String phaseTwoFilename = MultiwaySortMain.DEFAULT_TEMP_DATA_DIR + "PhaseTwo_";
	static int totalSortedNum = 0;

	static void phaseTwoMain() throws IOException {
		int finalOutputFileNum = totalFileNum;
		//System.out.println("Freemenunits: " + totalFreeMemUnits);

		while (finalOutputFileNum > 1) {
			int temItr = finalOutputFileNum / dataBufNum;
			int itrTimes = finalOutputFileNum % dataBufNum == 0 ? temItr : (temItr + 1);
			//System.out.println("databuf, current round: " + dataBufNum);
			//System.out.println("standard itr time: " + itrTimes);
			int leftFileNum = finalOutputFileNum % dataBufNum;
			//System.out.println("left file num: " + leftFileNum);
			numInFile = new int[totalFileNum];

			for (int i = 0; i < totalFileNum - 1; i++) {
				numInFile[i] = avgNumInFile;
			}
			numInFile[totalFileNum - 1] = lastNumInFile;

			for (int i = 0; i < itrTimes; i++) {
				sortedNumInFile = new int[totalFileNum];
				//System.out.println("round time: " + (i + 1));
				if (i == itrTimes - 1 && leftFileNum != 0) {
					bufUpperLimit = finalOutputFileNum % dataBufNum == 0 ? dataBufNum : finalOutputFileNum % dataBufNum;
				} else {
					bufUpperLimit = dataBufNum;
				}
				//System.out.println("Buffer upper limit: " + bufUpperLimit);
				initReadFile(fileName, bufUpperLimit, dataBufUnitSize, numInFile);
				outputFileCounter++;

				//System.out.println("Sorted num: " + totalSortedNum);
				totalSortedNumCounter = totalSortedNum;
				totalSortedNum = 0;
			}
			fileName = phaseTwoFilename;
			phaseTwoFilename += "x_";
			finalOutputFileNum = outputFileCounter;
			fileNumConter = finalOutputFileNum;
			totalFileNum = finalOutputFileNum;
			inputFileCounter = 0;
			outputFileCounter = 0;
			int temAvg = avgNumInFile;
			avgNumInFile = avgNumInFile * dataBufNum;
			lastNumInFile = temAvg * ((leftFileNum - 1 == 0 ? (dataBufNum - 1): leftFileNum - 1)) + lastNumInFile;
			//System.out.println("avanum: " + avgNumInFile + " , lastNum:" + lastNumInFile);
			//System.out.println("total sorted: " + totalSortedNumCounter);
		}
	}

	static void initReadFile(String filePrefix, int bufUpperLimit, int dataBufUnitSize, int[] numInThisFile)
			throws IOException {
		int[][] dataBuffer = new int[bufUpperLimit][dataBufUnitSize];

		File[] files = new File[bufUpperLimit];
		FileReader[] fileFRs = new FileReader[bufUpperLimit];
		BufferedReader[] fileBRs = new BufferedReader[bufUpperLimit];

		for (int i = 0; i < bufUpperLimit && (fileNumConter--) > 0; i++) {
			String curFilename = fileName + inputFileCounter + SUFFIX;
			files[i] = new File(curFilename);
			fileFRs[i] = new FileReader(files[i]);
			fileBRs[i] = new BufferedReader(fileFRs[i]);
			//System.out.println("Totalfilenum: " + fileNumConter);
			//System.out.println("Read from file " + curFilename);
			for (int j = 0; j < dataBufUnitSize; j++) {
				dataBuffer[i][j] = readOneNum(fileBRs[i]);
				// System.out.println("reading i: " + i + " , j:" + j);
			}
			inputFileCounter++;
		}
		sortAndMerge(fileBRs, bufUpperLimit, dataBufUnitSize, dataBuffer, numInThisFile);
		for (int i = 0; i < bufUpperLimit; i++) {
			fileFRs[i].close();
			fileBRs[i].close();
		}
	}

	static void sortAndMerge(BufferedReader[] fileBRs, int bufLimit, int dataBufSize, int[][] dataBuffer,
			int[] numInThisFile) throws IOException {
		int[] bufCurPosPointers = new int[bufLimit];
		int[] outputBuffer = new int[outputBufUnits];
		int outputBufCounter = 0;
		boolean isAllSorted = false;

		while (!isAllSorted) {
			int curMin = getMinNumPos(dataBuffer, bufCurPosPointers, fileBRs, numInThisFile);
			if (curMin != -1) {
				outputBuffer[outputBufCounter++] = curMin;
				totalSortedNum++;
				if (outputBufCounter == outputBufUnits) {
					String outputFilename = phaseTwoFilename + outputFileCounter;
					// totalSortedNum += outputBufCounter;
					//System.out.println("totalsortednum written to file(full): " + totalSortedNum + " ,sorted: " + totalSortedNum);
					// totalSortedNum);
					writeToFile(outputFilename, outputBuffer);
					outputBufCounter = 0;
				}
			} else {
				String outputFilename = phaseTwoFilename + outputFileCounter;
				// totalSortedNum += outputBufCounter;
				//System.out.println(
						//"totalsortednum written to file(last, unfull): " + totalSortedNum + " ,sorted: " + totalSortedNum);
				// totalSortedNum);
				int[] finalBuf = new int[outputBufCounter];
				for(int i = 0; i< outputBufCounter;i++) {
					finalBuf[i]= outputBuffer[i]; 
				}
				writeToFile(outputFilename, finalBuf);
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

	static int getMinNumPos(int[][] dataBuffer, int[] bufCurPosPointers, BufferedReader[] fileBRs, int[] numInThisFile)
			throws IOException {
		int dataUnitSize = dataBuffer[0].length;
		int[] firstNums = new int[bufUpperLimit];

		for (int i = 0; i < bufUpperLimit; i++) {
			firstNums[i] = dataBuffer[i][bufCurPosPointers[i]];
			// System.out.println("channel: " + i + ",curpos: " + bufCurPosPointers[i] +
			// "num: " + firstNums[i]);
		}

		int min = firstNums[0], minSeq = 0;
		for (int i = 1; i < bufUpperLimit; i++) {
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
			//System.out.println("channel: " + minSeq + ", sortednum: " + sortedNumInFile[minSeq] + ",numInfile: " + numInThisFile[minSeq]);
			sortedNumInFile[minSeq]++;
			// System.out.println("minseq:" + minSeq + ",sortednum:" +
			// sortedNumFromFileCounter[minSeq]);
			if (sortedNumInFile[minSeq] == numInThisFile[minSeq]) {
				bufCurPosPointers[minSeq] = 0;
				dataBuffer[minSeq][bufCurPosPointers[minSeq]] = -1;
				// System.out.println("channel: " + minSeq + " finished, pointer pos: " +
				// bufCurPosPointers[minSeq]);
			}
			if (bufCurPosPointers[minSeq] == dataUnitSize) {
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
		//System.out.println("Current seq: " + minSeq + ",min: " + min + ", sortednum: " + (++totalSortedNum));
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
