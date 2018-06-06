package com.hiramine.progressbartest;

// 何らかの処理クラス
public class SomethingProcess
{
	private static final long MILLIS_INTERVAL = 1 * 1000;	// 1秒
	private static final long MILLIS_FINISH = 10 * 1000;	// 1秒

	// 何らかの時間のかかる処理
	// この処理の仕様
	// ・10秒経過で処理完了する。
	// ・1秒経過ごとにプログレスリスナーを通して処理状況を更新する。
	// ・プログレスリスナーを通して処理を途中でキャンセル可能。
	public static int doSomething( OnProgressListener onProgressListener )
	{
		long start = System.currentTimeMillis();
		long next_checkmillis = start + MILLIS_INTERVAL;
		long finish_checkmillis = start + MILLIS_FINISH;

		while( true )
		{
			long current = System.currentTimeMillis();

			if( finish_checkmillis <= current )
			{	// 処理完了
				return 1;
			}

			if( next_checkmillis <= current)
			{	// チェック時間になったら進捗更新
				if( !onProgressListener.updateProgress( (int)(current - start) / 1000, (int)MILLIS_FINISH / 1000 ) )
				{    // ユーザー操作による処理中止
					return 0;
				}

				// 次のチェック時間の設定
				next_checkmillis += MILLIS_INTERVAL;
			}
		}
	}
}
