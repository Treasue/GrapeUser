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

import common.DBHelper;
import common.StringHelper;
import common.formHelper;
import database.db;
import interfaceApplication.user;
import net.bytebuddy.asm.Advice.Return;
import session.session;

public class userModel {
	private static DBHelper db;
	//private static String[] notnullfield = {"id","pw","name","registerip","wbid"};//不为空字段
	private formHelper _form;
	private userModel(){
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
	public JSONObject find_usernamebyID(String userName){
		JSONObject rs = db.eq("id", userName).find();
		return rs == null ? new JSONObject() : rs;
	}
	public JSONObject find_usernamebyEmail(String email){
		JSONObject rs = db.eq("email", email).find();
		return rs == null ? new JSONObject() : rs;
	}
	public JSONObject find_usernamebyMoblie(String phoneno){
		JSONObject rs = db.eq("mobphone", phoneno).find();
		return rs == null ? new JSONObject() : rs;
	}
	public boolean check_user(String id,String pw){
		return db.eq("id", id).eq("password", pw).find() == null;
	}
	
	public boolean check_username(String userName){
		String regex = "([a-z]|[A-Z]|[0-9]|[\\u4e00-\\u9fa5])+";
		Pattern p = Pattern.compile(regex);
		Matcher m = p.matcher(userName);
		return ( userName.length() >= 7 && userName.length() <= 15 ) && m.matches();
	}
	
	public Object register_username(JSONObject _userInfo){
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
	public JSONObject login_username(String userName,String userPassword,int loginMode){
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
		JSONObject rs = db.eq(_checkField, userPassword).eq("password", userPassword).find();
		if( rs != null){
			session sem = new session();
			rs.put("sid", sem.insertSession( userName,rs.toJSONString() ));
			;//增加登录日志
		}
		return rs;
	}
	
	public void logout_username(String uuid){
		session sem = new session();
		sem.deleteSession(uuid);
	}
	
	public long getpoint_username(String username){
		long rl = 0;
		JSONObject rs = db.eq("id", username).field("point").find();
		if( rs != null){
			rl = Long.parseLong( rs.get("point").toString() );
		}
		return rl;
	}
}