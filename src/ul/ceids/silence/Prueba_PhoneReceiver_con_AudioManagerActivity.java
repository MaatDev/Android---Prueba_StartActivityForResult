package ul.ceids.silence;

import android.app.Activity;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.provider.ContactsContract.CommonDataKinds.Phone;
import android.provider.ContactsContract.Contacts;
import android.provider.ContactsContract.Data;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

public class Prueba_PhoneReceiver_con_AudioManagerActivity extends Activity {
    /** Called when the activity is first created. */
	
	private final String TAG = getClass().getSimpleName();
	
	private EditText et_number;
	private SharedPreferences sp;
	private Editor et;
	
	public final static String DB_NAME = "MI_BLACK_LIST";
	public final static String DB_DATA = "SILENCE_NUMBER";
	
	private final static String NO_NUMBER = "No tiene número";
	
	public final static int REQUEST=0;
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        
        this.et_number = (EditText) findViewById(R.id.et_number);
        
        //Obtener base de datos NOSQL
                
        this.sp = this.getSharedPreferences(DB_NAME, Context.MODE_PRIVATE);
        
        this.et = sp.edit();

    }
    
       
    public void save(View v){
    	
    	String number = this.et_number.getText().toString();
    	
    	//Se coloca la llave del valor que se quiere guardar y el valor mismo
    	
    	this.et.putString(DB_DATA, number);
    	
    	this.et.commit();
    	
    	Toast.makeText(this.getApplicationContext(), "Número silenciado: "+number,
    		Toast.LENGTH_SHORT).show();
    	
    	//Registrar el BroadcastReceiver( si está registrado en el Manifest, se dispara aún que esté cerrado la aplicación)
  	
    	this.registerReceiver(new MyPhoneReceiver(), 
    		new IntentFilter(MyPhoneReceiver.ACTION));
    	
    }
    
    public void remove(View v){
    	
    	String number = this.sp.getString(DB_NAME, "No existe el número");
    	
    	//Eliminar la llave y su cotenido de la base de datos
    	
    	this.et.remove(DB_DATA);
    	
    	this.et.commit();
    	
    	Toast.makeText(this.getApplicationContext(), "Número borrado: "+number,
    		Toast.LENGTH_SHORT);
    	
    	//Si el broadcastreceiver no existe, lanza una excepción
    	
    	try{
    		this.unregisterReceiver(new MyPhoneReceiver());
    	}catch(Exception ex){
    		ex.printStackTrace();
    	}
    	    	
    }
    
    public void search(View v){
    	    	
    	this.startActivityForResult(new Intent(Intent.ACTION_PICK,ContactsContract.Contacts.CONTENT_URI), REQUEST);
    	
    }
    
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
    	// TODO Auto-generated method stub
//    	super.onActivityResult(requestCode, resultCode, data);
    	
    	//Si el usuairo no ha seleccionado ningún usuario, el resultCode será 0
    	
    	if(resultCode == 0){
    		
    		//No hacer nada
    		
    		return;
    		
    	}
    	
    	String s = searchContactNumber(data);
    	
    	this.et_number.setText(s);
    	
    }
    
    public String searchContactNumber(Intent intent){
    	
    	//Buscar el ID del contacto del cursor que se obtuvo del startActivityForResult
    	
    	Cursor cursor = managedQuery(intent.getData(), null, null, null, null);
    	
    	String number = NO_NUMBER;
    	

    	int hasPhoneNumber=0;
    	String lookupKey="";  //No es recomendable usar _id
    	
    	//Solo va iterar 1 vez, porque obtiene dato de 1 contacto
    	
    	while(cursor.moveToNext()){
    		
    		int indexHasPhoneNumber = cursor.getColumnIndex(ContactsContract.Contacts.HAS_PHONE_NUMBER);
    		int indexLookUpKey = cursor.getColumnIndex(ContactsContract.Contacts.LOOKUP_KEY);
    		    		
    		hasPhoneNumber = cursor.getInt(indexHasPhoneNumber);
    		lookupKey = cursor.getString(indexLookUpKey);


    	}    	
    	
    	//Verificar si el contacto tiene número de teléfono
    	   	
    	if(hasPhoneNumber != 0){
    		

    		cursor = getContentResolver().query(Phone.CONTENT_URI, null, Phone.LOOKUP_KEY + " = ?", new String[] { lookupKey }, null);


    		while(cursor.moveToNext()){
    			
    			number = cursor.getString(cursor.getColumnIndex(Phone.NUMBER));
    			
    		}
    		
    	}
    	
    	cursor.close();
    	
    	return number;
    	
    }
}