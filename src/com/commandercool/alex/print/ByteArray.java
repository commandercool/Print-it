package com.commandercool.alex.print;

public class ByteArray {
	private byte[] array;
	private int pos;
	private int size;
	public ByteArray(int size){
		array = new byte[size];
		pos = 0;
		this.size = size;
	}
	public int append(byte[] bytes, int len){
		if (len > (size - pos) ) return 0;
		for (int i = 0; i < len; i++){
			array[pos + i] = bytes[i];
		}
		pos +=len;
		return 1;
	}
	public byte[] getBytes(){
		return array;
	}
}
