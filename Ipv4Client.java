import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.Socket;

public class Ipv4Client {

	public static void main(String[] args) {
		try(Socket socket = new Socket("codebank.xyz", 38003)) {
			String address = socket.getInetAddress().getHostAddress();
			System.out.println("Connected to server.");

			//Create byte streams to communicate to Server
			InputStream is = socket.getInputStream();
			InputStreamReader isr = new InputStreamReader(is, "UTF-8");
			BufferedReader br = new BufferedReader(isr);
			OutputStream os = socket.getOutputStream();

			//If not work, make byte[] 20 + dataLength
			byte packet[] = new byte[20];

			for(int index=1; index < 13; index++) {
				//IPv4 Version
				int version = 4;

				//Header Length (in words)
				int HL = 5;

				//Concatenate for 1st byte of Packet
				packet[0] = (byte) ((version<<4 & 0xF0) | (HL & 0xF));

				//TOS (No implementation)
				int TOS = 0;
				packet[1] = (byte) TOS;

				//Total Length (Header + Data length in bytes)
				int dataLength = (int) Math.pow(2, index);
				int totalLength = (int) (20 + dataLength);
				byte lowerTotalLen = (byte) (totalLength & 0xFF);
				byte upperTotalLen = (byte) ((totalLength>>8) & 0xFF);

				packet[2] = (byte) upperTotalLen;
				packet[3] = (byte) lowerTotalLen;

				//Identification (No implementation)
				int ID = 0;
				packet[4] = (byte) ID;
				packet[5] = (byte) ID;

				//Flag is set to '010'
				//Fragment offset is 0 so adds 5 0's to the end of 010 = 01000000
				int flag = 64;
				packet[6] = (byte) flag;

				//Fragment Offset (No implementation)
				int fragOffset = 0;
				packet[7] = (byte) fragOffset;

				//Time to Live
				int TTL = 50;
				packet[8] = (byte) TTL;

				//TCP Protocol
				int TCP = 6;
				packet[9] = (byte) TCP;

				//CheckSum, set to 0
				int chkSum = 0;
				packet[10] = (byte) chkSum;
				packet[11] = (byte) chkSum;

				//Source/Sender IP Address
				int[] sourceAddress = {127, 0, 0, 1};
				for(int i=0; i < sourceAddress.length; i++) {
					packet[12+i] = (byte) sourceAddress[i];
				}

				//Destination/Receiver IP Address
				String[] temp = address.split("\\.");
				for(int i=0; i < temp.length; i++) {
					int val = Integer.valueOf(temp[i]);
					packet[16+i] = (byte) val;
				}

				//Print binary output of IPv4 pakcet headers
				/**
				System.out.println("0        8        16       24");
				int counter=1;
				for(byte b: packet) {
				    System.out.print(Integer.toBinaryString(b & 255 | 256).substring(1) + " ");
				    if(counter%4 ==0) {
				    	System.out.println();
				    }
				    counter++;
				}
				 */

				//Send byte array to checksum
				short returnVal = checkSum(packet);

				//Split checkSum return value, put into checksum
				int lowerBit = returnVal & 0xFF;
				int upperBit = returnVal>>8 & 0xFF;
				packet[10] = (byte) upperBit;
				packet[11] = (byte) lowerBit;

				//Write to Server
				for(byte b : packet) {
					os.write(b);
				}

				//Write data to Server
				System.out.println("Data length: " + dataLength);
				for(int i=0; i < dataLength; i++) {
					os.write(0);
				}

				System.out.println("Server Response: " + br.readLine() + "\n");
			}
		} catch(IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Helper Method - Performs checkSum
	 * */
	public static short checkSum(byte[] b) {
		int sum = 0;
		int i = 0;
		while(i < b.length-1) {
			byte first = b[i];
			byte second = b[i+1];
			sum += ((first<<8 & 0xFF00) | (second & 0xFF));

			if((sum & 0xFFFF0000) > 0) {
				sum &= 0xFFFF;
				sum++;
			}
			i = i + 2;
		}

		//If bArray size is odd
		if((b.length)%2 == 1) {
			byte last = b[(b.length-1)];
			sum += ((last<<8) & 0xFF00);

			if((sum & 0xFFFF0000) > 0) {
				sum &= 0xFFFF;
				sum++;
			}
		}
		return (short) ~(sum & 0xFFFF);
	}
}
