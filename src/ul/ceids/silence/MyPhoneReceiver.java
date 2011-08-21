package ul.ceids.silence;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.AudioManager;
import android.telephony.PhoneNumberUtils;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.widget.Toast;

public class MyPhoneReceiver extends BroadcastReceiver{

	private Context context;
	
	public final static String ACTION = TelephonyManager.ACTION_PHONE_STATE_CHANGED;
	
	private static final String DATA_NUMBER = "incoming_number";
	
	@Override
	public void onReceive(Context context, Intent data) {
		// TODO Auto-generated method stub
		
		this.context = context;
		
		//Obtener el número telefónico de la llamada recibida
		
		String incomingNumber = data.getExtras().getString(DATA_NUMBER);
		
		//Obtener el número registrado en la base de datos
		
		SharedPreferences sp = context.getSharedPreferences(
			Prueba_PhoneReceiver_con_AudioManagerActivity.DB_NAME, Context.MODE_PRIVATE);	
				
		String dbNumber = sp.getString(
			Prueba_PhoneReceiver_con_AudioManagerActivity.DB_DATA, "");
		
		//Comparar el número de la llamada con el número registrado con la utilidad de Android
		
		if(PhoneNumberUtils.compare(dbNumber, incomingNumber)){
			
			setSilence(context);
			
			//Obtener el servicio para leer el estado del celular y registrar el listener
			
			TelephonyManager manager = 
					(TelephonyManager)context.getSystemService(Context.TELEPHONY_SERVICE);
						
			manager.listen(myListener, PhoneStateListener.LISTEN_CALL_STATE);			
			
			Toast.makeText(context, "Número silenciado: "+incomingNumber,
		    		Toast.LENGTH_LONG).show();
			
		}
				
	}
	
	public void setSilence(Context context){
		
		//Sentencias para silenciar el celular
		
		AudioManager manager = 
			(AudioManager)context.getSystemService(Context.AUDIO_SERVICE);
		
		manager.setRingerMode(AudioManager.RINGER_MODE_SILENT);
		
	}
	
	public void setNormal(Context context){
		
		//Retorna al valor anterior antes de silenciar
		
		AudioManager manager = 
			(AudioManager)context.getSystemService(Context.AUDIO_SERVICE);
		
		manager.setRingerMode(AudioManager.RINGER_MODE_NORMAL);
		
	}
	
	private PhoneStateListener myListener = new PhoneStateListener(){
		
		//El listener para ver el estado del teléfono
		
		public void onCallStateChanged(int state, String incomingNumber) {
			
			if(state == TelephonyManager.CALL_STATE_IDLE){
				
				//Se termina la llamada
				
				setNormal(context);
				
			}			
			
		};
		
	};

}
