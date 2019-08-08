package learnjava.fun;

/*
 * Sorting a list of English words in ascending order
 * the USA Computing Olympiad (USACO)
 */
public class SortWordDictionary {

	public SortWordDictionary() {
		// TODO Auto-generated constructor stub
	}

	public static void main(String[] args) {
		//--------------Performance check----------------------//
		long startTime = System.currentTimeMillis();
		//-----------------------------------------------------//
		// define variables
		//define a String array which store 7 words
		String[] wordlist = {"RED","BLACK","WHITE","SILVER","ORANGE","BLUE"};
		//define an integer array to store the numbers. Each of them is mapped to a word.
		long[] numberlist = new long [wordlist.length];
		long number = 0;
		//define a temp string variable to store number of each word
		String temp = "";
		//loop through the string array to print out all 7 elements
		System.out.print("Before sorting wordlist=>");
		for(int i = 0;i<wordlist.length;i++) {
			System.out.print(wordlist[i]+"[");
			//convert each word to a char array which has all letters of each word
			char[] intuparray =wordlist[i].toCharArray();
			//loop through each char array which is mapped to each English word in wordlist[i]
			for(int j = 0;j<intuparray.length;j++) {
				//each letter has a unique two digits number
				//convert char to long and then append it to temp string variable
				number = (long)intuparray[j];
				temp = temp+number;
			}
			//save each number value of each word in numberlist array.
			//later on, we will sort this numberlist array.
			numberlist[i] = Long.parseLong(temp);
			System.out.print(numberlist[i]+"] ");
			temp = "";
		}
		//bubble sort number list
		long ntemp = 0; //temporary variable for swapping number purpose
		String stemp = ""; //temporary variable for swapping string purpose
		int counts = 0;
		int digitk = 0; //total digits of the current number
		int digitk2 = 0;//total digits of next number
		long currentnumber = 0;
		long nextnumber = 0;
		
		//bubble sorting starts
		for(int h = 0;h < (numberlist.length-1);h++) {
			
			/*this round of sorting only move the biggest bubble down to the bottom
			 *we swap N-1 times. Let's say if we have 7 elements in array - numberlist,
			 *we will swap at most 7-1 = 6 times in order to move the biggest bubble to
			 *the bottom
			 */
			for(int k = 0;k<(numberlist.length-1);k++) {
				//find out how many digits of current number and next number
				digitk = String.valueOf(numberlist[k]).length();
				digitk2 = String.valueOf(numberlist[k+1]).length();
				//print how many digits of the current number - k
				System.out.println("\n digitk="+digitk);
				//print how many digits of the next number - k+1
				System.out.println("digitk2="+digitk2);
				
				/*
				 * there is two conditions
				 * 1. current number has less digits than next number
				 * 2. current number has more digits than next number
				 * 
				 *  we can only compare two numbers when they all have same number
				 *  of digits. Need to append some zeros at the end of the number which
				 *  has less digits
				 */
				if((digitk-digitk2)<0) {
					//find the difference of total digits of current number and next number
					counts=(int)(digitk2-digitk);
					//assign the current number to temporary variable current number
					currentnumber = numberlist[k];
					//assign the next number to temporary variable next number
					nextnumber = numberlist[k+1];
					//append more zeros to the number having less digits
					for(int m=0;m<counts;m++) {
						currentnumber = currentnumber*10;
					}
				}else if((digitk-digitk2)>0){
					//find the difference of total digits of current number and next number
					counts=(int)(digitk-digitk2);
					//assign the current number to temporary variable current number
					currentnumber = numberlist[k];
					//assign the next number to temporary variable next number
					nextnumber = numberlist[k+1];
					//append more zeros to the number having less digits
					for(int m=0;m<counts;m++) {
						nextnumber = nextnumber*10;
					}
				} else {
					//current and next number both have same number of digits
					currentnumber=numberlist[k];
					nextnumber=numberlist[k+1];
				}
				System.out.println("currentnumber="+currentnumber+"["+wordlist[k]+"]");
				System.out.println("nextnumber="+nextnumber+"["+wordlist[k+1]+"]");
				if(currentnumber>nextnumber) {
					//swap two bubbles only if current is bigger than next one
					//please remember that we are doing ascending sorting
					ntemp = numberlist[k];
					numberlist[k] = numberlist[k+1];
					numberlist[k+1] = ntemp;
					//do the same swapping in wordlist array too because both numberlist array
					//and wordlist array have same data mapping - one number is mapped to one word
					stemp = wordlist[k];
					wordlist[k] = wordlist[k+1];
					wordlist[k+1] = stemp;
					System.out.println("need to swap because current is bigger than next");
				} else {
					System.out.println("Don't need to swap because current is smaller than next");
				}
			}
		}
		System.out.print("\n");
		//print out each element in numberlist array after sorting
		System.out.print("After sorting wordlist=>");
		for(int h = 0;h < numberlist.length;h++) {
			System.out.print(wordlist[h]+"[");
			System.out.print(numberlist[h]+"] ");
		}
		//--------------Performance check----------------------//
		long endTime = System.currentTimeMillis();
		long elapsedTime = (endTime - startTime);
		System.out.println("\nTotal Execution time : "+elapsedTime+ " milliseconds.");
		//-----------------------------------------------------//		
	}
}
