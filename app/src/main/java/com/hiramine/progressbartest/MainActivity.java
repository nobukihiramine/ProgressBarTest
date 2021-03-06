package com.hiramine.progressbartest;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.util.Locale;

public class MainActivity extends AppCompatActivity implements View.OnClickListener
{
	int iCounter;

	@Override
	protected void onCreate( Bundle savedInstanceState )
	{
		super.onCreate( savedInstanceState );
		setContentView( R.layout.activity_main );

		// プログレス関連コントロールの非表示
		findViewById( R.id.progresscontrols ).setVisibility( View.GONE );

		// スタートボタンのクリックリスナーの設定
		findViewById( R.id.buttonStart ).setOnClickListener( this );

		iCounter = 0;
	}

	@Override
	public void onClick( View v )
	{
		switch( v.getId() )
		{
			case R.id.buttonStart:
				// タスク実行
				String strTaskName = String.format( Locale.US, "Task%d", iCounter );
				MyAsyncTask myasynctask = new MyAsyncTask( this,
														   strTaskName,
														   (ViewGroup)findViewById( R.id.progresscontrols ),
														   (ProgressBar)findViewById( R.id.progressbarTask ),
														   (ImageButton)findViewById( R.id.imagebuttonCancel ),
														   (TextView)findViewById( R.id.textviewTaskName ),
														   (TextView)findViewById( R.id.textviewRate ),
														   (TextView)findViewById( R.id.textviewNumber ) );
				myasynctask.execute();
				//m_asynctask.executeOnExecutor( AsyncTask.SERIAL_EXECUTOR );
				iCounter++;
				break;
		}
	}
}
