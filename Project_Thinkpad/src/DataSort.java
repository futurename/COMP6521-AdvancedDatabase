public class DataSort {

	public static void mergeSort(int[] dataArray, int low, int mid, int high) {
		int n1 = mid - low + 1;
		int n2 = high - mid;

		int[] L = new int[n1];
		int[] R = new int[n2];

		for (int i = 0; i < n1; i++) {
			L[i] = dataArray[low + i];
		}
		for (int j = 0; j < n2; j++) {
			R[j] = dataArray[mid + 1 + j];
		}

		int i = 0, j = 0;
		int k = low;

		while (i < n1 && j < n2) {

			if (L[i] < R[j]) {

				dataArray[k] = L[i];
				i++;
			} else if (L[i] == R[j]) {
				dataArray[k] = L[i];
				k++;
				i++;
				dataArray[k] = R[j];
				j++;
			} else if (L[i] > R[j]) {
				dataArray[k] = R[j];
				j++;
			}
			k++;
		}

		while (i < n1) {
			dataArray[k] = L[i];
			k++;
			i++;
		}

		while (j < n2) {
			dataArray[k] = R[j];
			k++;
			j++;
		}
	}

	public static void sort(int[] array, int l, int r) {
		if (l < r) {
			int m = (l + r) / 2;
			sort(array, l, m);
			sort(array, m + 1, r);
			mergeSort(array, l, m, r);
		}
	}

}
