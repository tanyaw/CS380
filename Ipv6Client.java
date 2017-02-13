import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.Arrays;

public class Ipv6Client {

	public static void main(String[] args) {
		try(Socket socket = new Socket("codebank.xyz", 38004)) {
			String address = socket.getInetAddress().getHostAddress();
			System.out.println("Connected to server.");

			//Create byte streams to communicate to Server
			InputStream is = socket.getInputStream();
			OutputStream os = socket.getOutputStream();

			byte packet[] = new byte[40];

			//Send 12 Ipv6 packets
			for(int index=1; index < 13; index++) {
				//Ipv6 Version 0110 + 0000
				packet[0] = (byte) 96;

				//Set Traffic Class & Flow Label to 0
				//Values at packet[1] to packet[3] are initialized to 0

				//Payload Length (Data length in bytes)
				int dataLength = (int) Math.pow(2, index);
				byte lowerPayload = (byte) (dataLength & 0xFF);
				byte upperPayload = (byte) ((dataLength>>8) & 0xFF);

				packet[4] = upperPayload;
				packet[5] = lowerPayload;

				//Next Header is set to UDP
				packet[6] = (byte) 17;

				//Hop Limit is set to 20
				packet [7] = (byte) 20;

				//Set Source IP Address to local host 127.0.0.1
				//Extended address, leading 0's 
				//Values at packet[8] to packet[17] are initialized to 0
				packet[18] = (byte) 255;
				packet[19] = (byte) 255;
				packet[20] = (byte) 127;
				packet[23] = (byte) 1;

				//Destination/Receiver IP Address
				//Extended address, leading 0's 
				String[] temp = address.split("\\.");
				int destAddr[] = new int[4];

				//Cast address into Integer values
				for(int i=0; i < temp.length; i++) {
					int val = Integer.valueOf(temp[i]);
					destAddr[i] = val;
				}

				//Values at packet[24] to packet[33] are initialized to 0
				packet[34] = (byte) 255;
				packet[35] = (byte) 255;
				packet[36] = (byte) destAddr[0];
				packet[37] = (byte) destAddr[1];
				packet[38] = (byte) destAddr[2];
				packet[39] = (byte) destAddr[3];

				//Write to Server
				for(byte b : packet) {
					os.write(b);
				}

				//Write data to Server
				System.out.println("Data length: " + dataLength);
				for(int i=0; i < dataLength; i++) {
					os.write(0);
				}

				//Return value of 0xCAFEBABE indicates a good response
				System.out.print("Response: 0x");
				for(int j=0; j <4; j++) {
					System.out.printf("%02X", is.read());
				}
				System.out.println("\n");
			}
		} catch(IOException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Helper Method - Prints binary output of IPv6 packet headers
	 * */
	public static void printPacket() {
		System.out.println("0        8        16       24");
		int counter=1;
		for(byte b: packet) {
		    System.out.print(Integer.toBinaryString(b & 255 | 256).substring(1) + " ");
		    if(counter%4 ==0) {
		    	System.out.println();
		    }
		    counter++;
		}
	}
}
