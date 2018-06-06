package com.hiramine.progressbartest;

import android.content.Context;
import android.os.AsyncTask;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import java.lang.ref.WeakReference;

// AsyncTask<Params, Progress, Result>
public class MyAsyncTask extends AsyncTask<Void, Integer, Integer> implements OnProgressListener, View.OnClickListener
{
	// メンバー変数
	private WeakReference<Context>     m_weakrefContext;	// for Toast
	private WeakReference<ProgressBar> m_weakrefProgressBar;		// Progress Controls
	private WeakReference<ImageButton> m_weakrefImageButtonCancel;	// Progress Controls
	private WeakReference<TextView>    m_weakrefTextView;	// Progress Controls
	private String                     m_strTaskName;	// TaskName
	private boolean                    m_bInit;	// 初期化処理されたかフラグ

	// コンストラクタ
	public MyAsyncTask( Context context, ProgressBar progressBar, ImageButton buttonCancel, TextView textView, String strTaskName )
	{
		m_weakrefContext = new WeakReference<>( context );
		m_weakrefProgressBar = new WeakReference<>( progressBar );
		m_weakrefImageButtonCancel = new WeakReference<>( buttonCancel );
		m_weakrefImageButtonCancel = new WeakReference<>( buttonCancel );
		m_weakrefTextView = new WeakReference<>( textView );
		m_strTaskName = strTaskName;

		m_bInit = false;
	}

	// タスクの前処理
	// onPreExecute()は、メインスレッド（UIスレッド）で実行される。
	// タスクを連続実行した場合は、他のタスクの終了前に本処理が実行されるので、UIに関する処理はすべきでない。
	// （本処理にて、UIに関する処理を行うと、「前の処理のUI処理を書き換えてしまう」および「本処理のUI処理は前処理のUI処理によって書き換えられてしまう」となる）
	@Override
	protected void onPreExecute()
	{
	}

	// バックグラウンド処理
	// doInBackground()は、バックグラウンドスレッド（≠UIスレッド）で実行される。
	@Override
	protected Integer doInBackground( Void... params )
	{
		updateProgress( 0, 100 );	// プログレス状態の初期化
		return SomethingProcess.doSomething( this );	// 時間を要する処理
	}

	// プログレスの更新
	// onProgressUpdate()は、メインスレッド（UIスレッド）で実行される。
	// バックグラウンドスレッドの処理（doInBackground()内の処理）から、publishProgress()を呼び出すと、
	// UIスレッドでonProgressUpdate()が呼び出される。
	@Override
	protected void onProgressUpdate( Integer... progress )
	{
		if( !m_bInit )
		{
			// プログレス関連コントロールを表示にする。
			// タスクの連続実行の際に、前のタスクの終了時に非表示にしたものを再表示するために必要。
			setControlsVisibility( View.VISIBLE );

			// クリックリスナーの登録
			ImageButton imageButton = m_weakrefImageButtonCancel.get();
			if( null != imageButton )
			{
				imageButton.setOnClickListener( this );
			}

			// タスク名テキストの設定
			TextView textView = m_weakrefTextView.get();
			if( null != textView)
			{
				textView.setText( m_strTaskName );
			}

			m_bInit = true;
		}

		// プログレスバーの更新
		ProgressBar progressBar = m_weakrefProgressBar.get();
		if( null != progressBar )
		{
			progressBar.setMax( progress[1] );
			progressBar.setProgress( progress[0] );
		}
	}

	// タスクの後処理
	// onPostExecute()は、メインスレッド（UIスレッド）で実行される。
	@Override
	protected void onPostExecute( Integer result )
	{
		// プログレス関連コントロールを非表示にする。
		setControlsVisibility( View.GONE );

		// タスク完了後の処理
		Context context = m_weakrefContext.get();
		if( null != context )
		{
			if( 0 != result )
			{	// 処理成功
				Toast.makeText( context, "Finished. Succeeded.", Toast.LENGTH_SHORT ).show();
			}
			else
			{	// 処理失敗
				Toast.makeText( context, "Finished. Failed.", Toast.LENGTH_SHORT ).show();
			}
		}
	}

	// タスクがキャンセルされたときの処理
	// onCancelled()は、メインスレッド（UIスレッド）で実行される。
	// ※onCancelled()が呼ばれるタイミングは、doInBackground() の処理が完遂した後。
	// 　ユーザーがキャンセルしようがしまいが、doInBackground()は最後まで処理が遂行される。
	// 　doInBackground()が終わったあとに、
	// 　ユーザーがキャンセルしていなければ、onPostExecute()が呼ばれ、
	// 　ユーザーがキャンセルしていれば、onCancelled()が呼ばれる。
	@Override
	protected void onCancelled()
	{
		// プログレス関連コントロールを非表示にする。
		setControlsVisibility( View.GONE );

		// タスクキャンセル後の処理
		Context context = m_weakrefContext.get();
		if( null != context )
		{
			Toast.makeText( m_weakrefContext.get(), "Canceled.", Toast.LENGTH_SHORT ).show();
		}
	}

	// 進捗更新
	// OnProgressListenerインターフェースの関数の実装
	// updateProgress()は、バックグラウンドスレッド（doInBackground()内の処理）から呼び出す。
	public boolean updateProgress( int iPos, int iMax )
	{
		publishProgress( iPos, iMax );    // publishProgress()を呼び出すと、UIスレッドでonProgressUpdate()が呼び出される。
		return !isCancelled();    // 処理続行か否か（処理続行:true, 処理中止:false）
	}

	// クリックイベントハンドラ
	// onClick()は、メインスレッド（UIスレッド）で実行される。
	@Override
	public void onClick( View v )
	{
		ImageButton imageButton = m_weakrefImageButtonCancel.get();
		if( null == imageButton )
		{
			return;
		}

		if( imageButton.getId() == v.getId() )
		{
			// キャンセル
			cancel( true );
		}
	}

	// プログレス関連コントロールの表示/非表示
	private void setControlsVisibility( int iVisibility )
	{
		ProgressBar progressBar = m_weakrefProgressBar.get();
		if( null != progressBar )
		{
			progressBar.setVisibility( iVisibility );
		}
		ImageButton imageButton = m_weakrefImageButtonCancel.get();
		if( null != imageButton )
		{
			imageButton.setVisibility( iVisibility );
		}
		TextView textView = m_weakrefTextView.get();
		if( null != textView )
		{
			textView.setVisibility( iVisibility );
		}
	}
}
