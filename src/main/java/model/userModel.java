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
	private int errno;
	/**
	 * 用户注册
	 * @param _userInfo
	 * @return		大于 0:返回用户 ID，表示用户注册成功
	 * 				mongodb 返回 objectid string类型
					-1:用户名不合法
					-2:包含不允许注册的词语
					-3:用户名已经存在
					-4:Email 格式有误
					//-5:Email 不允许注册
					-6:该 Email 已经被注册
					-7:提交的数据包含不合法的数据
					-8:不存在用户EMAIL或者手机号
					-9:必填数据没有填
	 */
	public Object user_register(JSONObject _userInfo){
		int chkcode = _form.check_forminfo(_userInfo);
		if( chkcode == 1 ){
			return 9;
		}
		if( chkcode == 2 ){
			return 8;
		}
		String userName = _userInfo.get("id").toString();
		if( !check_username(userName) ){
			return 1;
		}
		if( find_usernamebyID(userName) != null){
			return 3;
		}
		String email = _userInfo.get("email").toString();
		if(!StringHelper.checkEmail(email) ){
			return 4;
		}
		if( find_usernamebyEmail(email) != null){
			return 6;
		}
		return db.data(_userInfo).insertOnce().toString();
	}
	
	public JSONObject find_usernamebyID(String userName){
		return db.eq("id", userName).find();
	}
	public JSONObject find_usernamebyEmail(String email){
		return db.eq("email", email).find();
	}
	
	private boolean check_username(String userName){
		String regex = "([a-z]|[A-Z]|[0-9]|[\\u4e00-\\u9fa5])+";
		Pattern p = Pattern.compile(regex);
		Matcher m = p.matcher(userName);
		return ( userName.length() >= 7 && userName.length() <= 15 ) && m.matches();
	}
	

}