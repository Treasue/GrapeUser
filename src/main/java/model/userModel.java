/**
 * 
 */
/**
 * @author Administrator
 *
 */
package model;

import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.json.simple.JSONObject;

import database.db;
import esayhelper.DBHelper;
import esayhelper.formHelper;
import interfaceApplication.user;
import security.codec;
import session.session;

public class userModel {
	private static DBHelper db;
	//private static String[] notnullfield = {"id","pw","name","registerip","wbid"};//不为空字段
	private formHelper _form;
	public userModel(){
		_form = new formHelper();
		_form.addNotNull("id,pw,name,registerip,wbid");
		HashMap<String, Object> checkfield = new HashMap<String, Object>();
		checkfield.put("email", "");
		checkfield.put("mobphone", "");
		_form.addMustHaveOne(checkfield);
		HashMap<String, Object> defcol = new HashMap<String, Object>();
		defcol.put("uid", null);
		defcol.put("sex", 1);
		defcol.put("birthday", 0);
		defcol.put("point", 0);
		defcol.put("cash", 0.0);
		defcol.put("ownid", 0);
		defcol.put("time", null);
		defcol.put("lasttime", 0);
		defcol.put("ugid", 0);
		defcol.put("state", 0);
		defcol.put("isdelete", 0);
		defcol.put("isvisble", 0);
		_form.adddef(defcol);
	}
	static{
		db = new DBHelper("mongodb", "user");
	}
	
	public db getDB(){
		return db;
	}
	public JSONObject findUserNameByID(String userName){
		JSONObject rs = db.eq("id", userName).find();
		return rs == null ? new JSONObject() : rs;
	}
	public JSONObject findUserNameByEmail(String email){
		JSONObject rs = db.eq("email", email).find();
		return rs == null ? new JSONObject() : rs;
	}
	public JSONObject findUserNameByMoblie(String phoneno){
		JSONObject rs = db.eq("mobphone", phoneno).find();
		return rs == null ? new JSONObject() : rs;
	}
	public boolean checkUser(String id,String pw){
		pw = secPassword( pw.toString() );
		return db.eq("id", id).eq("password", pw).find() == null;
	}
	
	public boolean checkUserName(String userName){
		String regex = "([a-z]|[A-Z]|[0-9]|[\\u4e00-\\u9fa5])+";
		Pattern p = Pattern.compile(regex);
		Matcher m = p.matcher(userName);
		return ( userName.length() >= 7 && userName.length() <= 15 ) && m.matches();
	}
	
	@SuppressWarnings("unchecked")
	public Object registerUsername(JSONObject _userInfo){
		String secpassword = secPassword(_userInfo.get("password").toString());
		_userInfo.replace("password", secpassword);
		return db.data(_userInfo).insertOnce().toString();
	}
	
	/**
	 * @param userName
	 * @param userPassword
	 * @param loginMode
	 * 			0：用户名登录
	 * 			1:email登录
	 * 			2：手机号登录
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public JSONObject loginUsername(String userName,String userPassword,int loginMode){
		String _checkField = "";
		switch(loginMode){
		case 0:
			_checkField = "id";
			break;
		case 1:
			_checkField = "email";
			break;
		case 2:
			_checkField = "mobphone";
			break;
		}
		userPassword = codec.md5(userPassword);
		JSONObject rs = db.eq(_checkField, userPassword).eq("password", userPassword).find();
		if( rs != null){
			session sem = new session();
			rs.put("sid", sem.insertSession( userName,rs.toJSONString() ));
			;//增加登录日志
		}
		return rs;
	}
	
	public void logoutUsername(String uuid){
		session sem = new session();
		sem.deleteSession(uuid);
	}
	
	public long getPointUsername(String username){
		long rl = 0;
		JSONObject rs = db.eq("id", username).field("point").find();
		if( rs != null){
			rl = Long.parseLong( rs.get("point").toString() );
		}
		return rl;
	}
	private String secPassword(String password){
		return codec.md5( password );
	}
	public String select() {
		return db.select().toString();
	}

	public int batch(String[] arr) {
		db = (DBHelper) db.or();
		for (String string : arr) {
			db.eq("_id", string);
		}
		return db.delete()!= null ? 0 : 99;
	}
}