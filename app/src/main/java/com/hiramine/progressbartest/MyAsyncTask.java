package com.hiramine.progressbartest;

import android.content.Context;
import android.os.AsyncTask;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import java.lang.ref.WeakReference;
import java.util.Locale;

// AsyncTask<Params, Progress, Result>
public class MyAsyncTask extends AsyncTask<Void, Integer, Integer> implements OnProgressListener, View.OnClickListener
{
	// メンバー変数
	// WeakReferenceを使用している理由。
	// 　Viewオブジェクトは、Contextオブジェクトを保持している。
	// 　AsyncTaskオブジェクトに、Viewオブジェクトをそのまま保持させると、
	// 　「Viewは破棄されているが、タスクはまだ完了していない」場合に、「Contextオブジェクトは、開放されずメモリリークする」。
	// 　WeakReferenceを使用し、「Viewは破棄されているが、タスクはまだ完了していない」場合でも、「Contextオブジェクトが開放される」ようにする。
	// 　see. https://stackoverflow.com/questions/37531862/how-to-pass-context-to-asynctask/37531974
	private final WeakReference<Context>     m_weakrefContext;    // for Toast
	private final String                     m_strTaskName;    // TaskName
	private final WeakReference<ViewGroup>   m_weakrefProgressControls;    // 進捗コントロール群
	private final WeakReference<ProgressBar> m_weakrefProgressBarTask;    // プログレスバー
	private final WeakReference<ImageButton> m_weakrefImageButtonCancel; // キャンセルボタン
	private final WeakReference<TextView>    m_weakrefTextViewTaskName;    // タスク名テキストビュー
	private final WeakReference<TextView>    m_weakrefTextViewRate;    // 進捗率テキストビュー
	private final WeakReference<TextView>    m_weakrefTextViewNumber;    // 進捗数テキストビュー
	private       boolean                    m_bInit;    // 初期化処理されたかフラグ

	// コンストラクタ
	public MyAsyncTask( Context context,
						String strTaskName,
						ViewGroup progresscontrols,
						ProgressBar progressbarTask,
						ImageButton imagebuttonCancel,
						TextView textviewTaskName,
						TextView textviewRate,
						TextView textviewNumber )
	{
		// タスク固有メンバー
		m_weakrefContext = new WeakReference<>( context );
		m_strTaskName = strTaskName;

		// プログレスコントロール関連
		m_weakrefProgressControls = new WeakReference<>( progresscontrols );
		m_weakrefProgressBarTask = new WeakReference<>( progressbarTask );
		m_weakrefImageButtonCancel = new WeakReference<>( imagebuttonCancel );
		m_weakrefTextViewTaskName = new WeakReference<>( textviewTaskName );
		m_weakrefTextViewRate = new WeakReference<>( textviewRate );
		m_weakrefTextViewNumber = new WeakReference<>( textviewNumber );

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
		updateProgress( 0, 100 );    // プログレス状態の初期化
		return SomethingProcess.doSomething( this );    // 時間を要する処理
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
			ViewGroup progresscontrols = m_weakrefProgressControls.get();
			if( null != progresscontrols )
			{
				progresscontrols.setVisibility( View.VISIBLE );
			}

			// クリックリスナーの登録
			ImageButton imageButton = m_weakrefImageButtonCancel.get();
			if( null != imageButton )
			{
				imageButton.setOnClickListener( this );
			}

			// タスク名テキストの設定
			TextView textView = m_weakrefTextViewTaskName.get();
			if( null != textView )
			{
				textView.setText( m_strTaskName );
			}

			m_bInit = true;
		}

		// プログレスバーの更新
		ProgressBar progressBar = m_weakrefProgressBarTask.get();
		if( null != progressBar )
		{
			progressBar.setMax( progress[1] );
			progressBar.setProgress( progress[0] );
		}

		TextView textView;
		// 進捗率テキストの設定
		textView = m_weakrefTextViewRate.get();
		if( null != textView )
		{
			textView.setText( String.format( Locale.US, "%d%%", Math.round( ( progress[0] * 100.0f ) / progress[1] ) ) );
		}

		// 進捗数テキストの設定
		textView = m_weakrefTextViewNumber.get();
		if( null != textView )
		{
			textView.setText( String.format( Locale.US, "%d/%d", progress[0], progress[1] ) );
		}
	}

	// タスクの後処理
	// onPostExecute()は、メインスレッド（UIスレッド）で実行される。
	@Override
	protected void onPostExecute( Integer result )
	{
		// タスク完了後の処理
		Context context = m_weakrefContext.get();
		if( null != context )
		{
			if( 0 != result )
			{    // 処理成功
				Toast.makeText( context, m_strTaskName + " : Succeeded.", Toast.LENGTH_SHORT ).show();
			}
			else
			{    // 処理失敗
				Toast.makeText( context, m_strTaskName + " : Failed.", Toast.LENGTH_SHORT ).show();
			}
		}

		// プログレス関連コントロールを非表示にする。
		ViewGroup progresscontrols = m_weakrefProgressControls.get();
		if( null != progresscontrols )
		{
			progresscontrols.setVisibility( View.GONE );
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
		// タスクキャンセル後の処理
		Context context = m_weakrefContext.get();
		if( null != context )
		{
			Toast.makeText( m_weakrefContext.get(), m_strTaskName + " : Canceled.", Toast.LENGTH_SHORT ).show();
		}

		// プログレス関連コントロールを非表示にする。
		ViewGroup progresscontrols = m_weakrefProgressControls.get();
		if( null != progresscontrols )
		{
			progresscontrols.setVisibility( View.GONE );
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
		if( null != imageButton )
		{
			if( imageButton.getId() == v.getId() )
			{
				// キャンセル
				cancel( true );
			}
		}
	}
}
