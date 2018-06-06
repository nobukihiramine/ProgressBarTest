package com.hiramine.progressbartest;

// 進捗表示用のリスナーインターフェース
public interface OnProgressListener
{
	boolean updateProgress( int iPos, int iMax );
}
