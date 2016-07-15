package com.uestc.login;


import java.io.File;

import org.json.JSONException;
import org.json.JSONObject;

import com.facepp.error.FaceppParseException;
import com.facepp.http.HttpRequests;
import com.facepp.http.PostParameters;


import android.app.Activity;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

public class processActivity extends Activity {
	
	
	private Thread thread;
	private ImageView imageView;
	private Button button5;
	Bitmap bm = null;
	String userID;
	
	@Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
		this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_process);
        
        
        Intent intent=getIntent();
        if(intent != null){
        	userID = intent.getStringExtra("user");
        	//Toast.makeText(getApplication(), userID, Toast.LENGTH_LONG).show();
        }
        else{
        	Intent intent_back = new Intent(processActivity.this, loginActivity.class);
        	intent_back.putExtra("user", userID);
			startActivity(intent_back);
			finish();
        }
        
        button5 = (Button)findViewById(R.id.button5);
        button5.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				Intent intent_back = new Intent(processActivity.this, loginActivity.class);
				intent_back.putExtra("user", userID);
				startActivity(intent_back);
				finish();
			}
		});
        
        imageView = (ImageView)findViewById(R.id.imageView1);
        File file = new File("/sdcard/1.jpg");
        if(file.exists()){
        	//Toast.makeText(getApplication(), "正在分析图片\n请保证网络流畅", Toast.LENGTH_SHORT).show();
        	bm = BitmapFactory.decodeFile("/sdcard/1.jpg");
        	imageView.setImageBitmap(bm);
        }
        else{
        	Intent intent_back = new Intent(processActivity.this, loginActivity.class);
        	intent_back.putExtra("user", userID);
			startActivity(intent_back);
        }
        thread = new Thread(runnable);
		thread.start();
		
	}
	
	JSONObject result;
	public void detectResult(JSONObject rst) {
		
		//use the red paint
		Paint paint = new Paint();
		paint.setColor(Color.RED);
		paint.setStrokeWidth(Math.max(bm.getWidth(), bm.getHeight()) / 100f);

		//create a new canvas
		Bitmap bitmap = Bitmap.createBitmap(bm.getWidth(), bm.getHeight(), bm.getConfig());
		Canvas canvas = new Canvas(bitmap);
		canvas.drawBitmap(bm, new Matrix(), null);
		
		
		try {
			//find out all faces
			final int count = rst.getJSONArray("face").length();
			for (int i = 0; i < count; ++i) {
				float x, y, w, h;
				//get the center point
				//x = (float)rst.get("face").get(i).get("position").get("center").get("x").toDouble().doubleValue();
				//y = (float)rst.get("face").get(i).get("position").get("center").get("y").toDouble().doubleValue();
				x = (float)rst.getJSONArray("face").getJSONObject(i).getJSONObject("position").getJSONObject("center").getDouble("x");
				y = (float)rst.getJSONArray("face").getJSONObject(i).getJSONObject("position").getJSONObject("center").getDouble("y");

				//get face size
				//w = (float)rst.get("face").get(i).get("position").get("width").toDouble().doubleValue();
				//h = (float)rst.get("face").get(i).get("position").get("height").toDouble().doubleValue();
				w = (float)rst.getJSONArray("face").getJSONObject(i).getJSONObject("position").getDouble("width");
				h = (float)rst.getJSONArray("face").getJSONObject(i).getJSONObject("position").getDouble("height");
				
				//change percent value to the real size
				x = x / 100 * bm.getWidth();
				w = w / 100 * bm.getWidth()*0.7f;
				y = y / 100 * bm.getHeight();
				h = h / 100 * bm.getHeight()*0.7f;

				//draw the box to mark it out
				canvas.drawLine(x - w, y - h, x - w, y + h, paint);
				canvas.drawLine(x - w, y - h, x + w, y - h, paint);
				canvas.drawLine(x + w, y + h, x - w, y + h, paint);
				canvas.drawLine(x + w, y + h, x + w, y - h, paint);
			}
			//save new image
			bm = bitmap;
		}catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	};
	
	
	
	Runnable runnable = new Runnable() {
		public void run() {
		HttpRequests httpRequests = new HttpRequests("270ff76b7c212533a3079aa0be82a3c5","hsWv6TPO6nc_PteGNxAPMwihBEBQQpYz");
		//final FaceppResult result;
		try {
			//PostParameters postParameters =new PostParameters();
			File file = new File("/sdcard/1.jpg");
			/*postParameters.setImg(file);
			postParameters.getMultiPart().writeTo(System.out);*/
			result = httpRequests.detectionDetect(new PostParameters().setImg(file));
			System.out.println(result);
			
		} catch (FaceppParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			processActivity.this.runOnUiThread(new Runnable() {
				
				public void run() {
					Toast.makeText(getApplication(), "网络问题！\n请保证网络流畅！", Toast.LENGTH_LONG).show();
				}
			});
		}
		detectResult(result);
		processActivity.this.runOnUiThread(new Runnable() {
			public void run() {
				//show the image
				imageView.setImageBitmap(bm);
				//Toast.makeText(getApplication(), "Success！", Toast.LENGTH_SHORT).show();
			}
		});
		
		int number = -1;
		/*try {
			//number = result.get("face").getCount();
			number = result.getJSONArray("face").length();
		} catch (FaceppParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}*/
		try {
			number = result.getJSONArray("face").length();
		} catch (JSONException e2) {
			// TODO Auto-generated catch block
			e2.printStackTrace();
		}
		if(number ==0){
			processActivity.this.runOnUiThread(new Runnable() {
				public void run() {
					Toast.makeText(getApplication(), "没有检测到人脸，请重试！", Toast.LENGTH_LONG).show();
				}
			});
		}
		else if (number > 1) {
			processActivity.this.runOnUiThread(new Runnable() {
				public void run() {
					Toast.makeText(getApplication(), "检测到多个人脸，请重试！", Toast.LENGTH_LONG).show();
				}
			});
		}
		else if (number == 1) {
			String face_id =null;
			/*try {
				//face_id = result.get("face").get(0).get("face_id").toString();
				System.out.println(face_id);
			} catch (FaceppParseException e2) {
				// TODO Auto-generated catch block
				e2.printStackTrace();
			}*/
			try {
				face_id = result.getJSONArray("face").getJSONObject(0).getString("face_id");
			} catch (JSONException e2) {
				// TODO Auto-generated catch block
				e2.printStackTrace();
			}
			//Create_person
			try {
				result = httpRequests.personCreate(new PostParameters().setPersonName(userID));
				System.out.println(result);
			} catch (FaceppParseException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			try {
				result = httpRequests.personGetInfo(new PostParameters().setPersonName(userID));
				System.out.println(result);
				int num_face = -1;
				//num_face = result.get("face").getCount();
					num_face = result.getJSONArray("face").length();
				if(num_face<5){
					result = httpRequests.personAddFace(new PostParameters().setPersonName(userID).setFaceId(face_id));
					System.out.println(result);
					processActivity.this.runOnUiThread(new Runnable() {
						public void run() {
							Toast.makeText(getApplication(), "人脸建模需要\n五张人脸数据\n请返回继续拍照", Toast.LENGTH_LONG).show();
						}
					});
				}
				else if(num_face == 5){
					result = httpRequests.trainVerify(new PostParameters().setPersonName(userID));
					//result = httpRequests.train(new PostParameters().setPersonName(userID));
					System.out.println(result);
					//String session_id = result.get("session_id").toString();
					String session_id = result.getString("session_id");
					processActivity.this.runOnUiThread(new Runnable() {
						public void run() {
							Toast.makeText(getApplication(), "   正在建模中    \n请耐心等待训练完成！", Toast.LENGTH_LONG).show();
						}
					});
					while (true) {
						result = httpRequests.infoGetSession(new PostParameters().setSessionId(session_id));
						if((result.getString("status")).equals("SUCC")){
							result = httpRequests.personAddFace(new PostParameters().setPersonName(userID).setFaceId(face_id));
							System.out.println(result);
							processActivity.this.runOnUiThread(new Runnable() {
								public void run() {
									Toast.makeText(getApplication(), "训练完成，返回登陆", Toast.LENGTH_LONG).show();
								}
							});
							break;
						}
					}
				}
				else {
					result = httpRequests.recognitionVerify(new PostParameters().setPersonName(userID).setFaceId(face_id));
					if(result.getBoolean("is_same_person")){
						processActivity.this.runOnUiThread(new Runnable() {
							public void run() {
								Toast.makeText(getApplication(), "登陆成功", Toast.LENGTH_LONG).show();
							}
						});
					}
					else{
						processActivity.this.runOnUiThread(new Runnable() {
							public void run() {
								Toast.makeText(getApplication(), "登陆失败", Toast.LENGTH_LONG).show();
							}
						});
					}
				}
				
			} catch (FaceppParseException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				processActivity.this.runOnUiThread(new Runnable() {
					
					public void run() {
						Toast.makeText(getApplication(), "网络问题！\n请保证网络流畅！", Toast.LENGTH_LONG).show();
					}
				});
			}catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		
		
		
		}
	};
	
	
	
	/*private Handler handler = new Handler(){
		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case MSG_SUCCESS:
				detectResult(result);
				imageView.setImageBitmap(bm);
				int number = -1;
				try {
					number = result.get("face").getCount();
				} catch (FaceppParseException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				if(number ==0)
					Toast.makeText(getApplication(), "没有检测到人脸，请重试！", Toast.LENGTH_LONG).show();
				else if (number > 1) {
					Toast.makeText(getApplication(), "检测到多个人脸，请重试！", Toast.LENGTH_LONG).show();
				}
				else if (number == 1) {
					//分析activity
					
					String face_id =null;
					try {
						face_id = result.get("face").get(0).get("face_id").toString();
						System.out.println(face_id);
					} catch (FaceppParseException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					Toast.makeText(getApplication(), face_id, Toast.LENGTH_LONG).show();
					
					String gender = null;
					try {
						gender = result.get("face").get(0).get("attribute").get("gender").get("value", JsonType.STRING).toString();
					} catch (FaceppParseException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					
					if(gender.equals("Female")){
						Toast toast = Toast.makeText(getApplication(), "我猜你是女的！", Toast.LENGTH_SHORT);
						toast.setGravity(Gravity.CENTER, 0, 0);
						toast.show();
					}
					else {
						Toast toast = Toast.makeText(getApplication(), "我猜你是男的！", Toast.LENGTH_SHORT);
						toast.setGravity(Gravity.CENTER, 0, 0);
						toast.show();
					}
					
					String age = null;
					String range = null;
					try {
						age = result.get("face").get(0).get("attribute").get("age").get("value",JsonType.STRING).toString();
						range = result.get("face").get(0).get("attribute").get("age").get("range",JsonType.STRING).toString();
					} catch (FaceppParseException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					Toast toast = Toast.makeText(getApplication(), "我猜你："+age+" ± "+range+"岁", Toast.LENGTH_SHORT);
					toast.setGravity(Gravity.CENTER, 0, 0);
					toast.show();
					
				}
				else {
					Toast.makeText(getApplication(), "未知问题，请重试！", Toast.LENGTH_LONG).show();
				}
				
				
				break;
				
			case MSG_FAILURE:
				Toast.makeText(getApplication(), "网络问题！", Toast.LENGTH_LONG).show();

			default:
				break;
			}
			
		};
		
		
	};*/
	
	
	

}
