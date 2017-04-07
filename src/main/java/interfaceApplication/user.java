 /**
 * 用户子系统接口类,控制器
 */
/**
 * @author Administrator
 *
 */
package interfaceApplication;

import java.util.HashMap;
import java.util.Set;

import org.bson.types.ObjectId;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import esayhelper.JSONHelper;
import esayhelper.StringHelper;
import esayhelper.formHelper;
import esayhelper.jGrapeFW_Message;
import model.userModel;
import security.codec;

public class user{
	private userModel user = new userModel();
	private formHelper _form;
	public user(){
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
	public String UserRegister(String userInfo){
		JSONObject _userInfo = JSONHelper.string2json(userInfo);
		int chkcode = _form.check_forminfo(_userInfo);
		if( chkcode == 1 ){
			return resultMessage(9);
		}
		if( chkcode == 2 ){
			return resultMessage(8);
		}
		String userName = _userInfo.get("id").toString();
		if( !user.checkUserName(userName) ){
			return resultMessage(1);
		}
		if( user.findUserNameByID(userName) != null){
			return resultMessage(3);
		}
		String email = _userInfo.get("email").toString();
		if(!StringHelper.checkEmail(email) ){
			return resultMessage(4);
		}
		if( user.findUserNameByEmail(email) != null){
			return resultMessage(5);
		}
		String phoneno = _userInfo.get("mobphone").toString(); 
		if(!StringHelper.checkMobileNumber(phoneno) ){
			return resultMessage(7);
		}
		if( user.findUserNameByMoblie(phoneno) != null){
			return resultMessage(6);
		}
		return resultMessage(0,user.registerUsername(_userInfo).toString());
	}
	
	/**
	 * @param userInfo
	 * username 	登录名
	 * password 	登录密码
	 * loginmode	登录模式
	 * 			0：用户名登录
	 * 			1:email登录
	 * 			2：手机号登录
	 * @return 除了密码以外的全部数据
	 */
	public String UserLogin(String userInfo){
		JSONObject _userInfo = JSONHelper.string2json(userInfo);
		int loginMode = 0;
		String userName = "";
		if( _userInfo.containsKey("loginmode") ){
			loginMode = Integer.parseInt(_userInfo.get("loginmode").toString());
		}
		if( loginMode == 0 ){
			userName = _userInfo.get("id").toString();
			if(!user.checkUserName(_userInfo.get("id").toString())){
				return resultMessage(1);
			}
		}
		if( loginMode == 1){
			userName = _userInfo.get("email").toString();
			if( !StringHelper.checkEmail(_userInfo.get("email").toString())){
				return resultMessage(4);
			}
		}
		if( loginMode == 2 ){
			userName = _userInfo.get("mobphone").toString();
			if(!StringHelper.checkMobileNumber(_userInfo.get("mobphone").toString())){
				return resultMessage(7);
			}
		}
		String password = "";
		if( _userInfo.containsKey("loginmode") && password.length() < 3){
			return resultMessage(10);
		}
		JSONObject uobj = user.loginUsername(userName, password, loginMode);
		if( uobj != null ){
			uobj.remove("password");
		}
		return uobj.toJSONString();
	}
	
	/**用户退出
	 * @param uuid
	 * @return
	 */
	public String UserLogout(String uuid){
		if( uuid.length() > 1 && uuid.length() < 128 ){
			user.logoutUsername(uuid);
		}
		return resultMessage(0,"退出成功");
	}
	
	/**获得积分
	 * @param id
	 * @return
	 */
	public String UserGetPoint(String id){
		return String.valueOf(user.getPointUsername(id)); 
	}
	
	/**
	 * @param moblie 手机号
	 * @return
	 */
	public String UserGetMoblie(int moblie){
		return user.findUserNameByMoblie(String.valueOf(moblie) ).toJSONString();
	}
	
	/**
	 * @param id 用户ID
	 * @return
	 */
	public String UserGet(String id){
		return user.findUserNameByID(id).toJSONString(); 
	}
	
	/**
	 * @param email email地址
	 * @return
	 */
	public String UserGetEmail(String email){
		return user.findUserNameByEmail(email).toJSONString();
	}
	
	/**修改密码
	 * @param id
	 * @param oldPW
	 * @param newPW
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public String UserChangePW(String id,String oldPW,String newPW){
		JSONObject ndata;
		if( !user.checkUser(id,oldPW) ){
			return resultMessage(11);
		}
		ndata = new JSONObject();
		ndata.put("password", codec.md5(newPW));
		ndata = user.getDB().eq("id", id).eq("password", oldPW).data(ndata).update();
		return resultMessage( ndata != null ? 0 : 99,"密码修改成功");
	}
	/**更新用户信息
	 * @param userInfo
	 * @return
	 */
	public String UserEdit(String id,String userInfo){
		JSONObject _userInfo = JSONHelper.string2json(userInfo);
		if( _userInfo.containsKey("id")){
			if( !user.checkUserName(_userInfo.get("id").toString()) ){
				return resultMessage(1);
			}
		}
		if( !_form.check_notnull_safe(_userInfo) ){//不为空的字段的检查
			return resultMessage(1);
		}
		if( _userInfo.containsKey("email")){
			if( ! StringHelper.checkEmail(_userInfo.get("email").toString()) ){
				return resultMessage(4);
			}
		}
		if( _userInfo.containsKey("mobphone")){
			if( ! StringHelper.checkMobileNumber(_userInfo.get("mobphone").toString()) ){
				return resultMessage(7);
			}
		}
		return resultMessage( user.getDB().eq("_id", new ObjectId(id)).data(userInfo).update() != null ? 0 : 99,"更新用户数据成功");
//		return resultMessage(( _userInfo.containsKey("id") && _userInfo.containsKey("password") && user.checkUser(_userInfo.get("id").toString(), _userInfo.get("password").toString()) ) && user.getDB().data(userInfo).update() != null ? 0 : 99,"更新用户数据成功");
	}
	
	public String UserDelete(String userid) {
		return resultMessage(user.getDB().eq("_id", new ObjectId(userid)).delete()!=null?0:99, "删除用户成功");
	}
	public String UserSearch(String userinfo) {
		JSONObject object = JSONHelper.string2json(userinfo);
		@SuppressWarnings("unchecked")
		Set<Object> set = object.keySet();
		for (Object object2 : set) {
			user.getDB().eq(object2.toString(), object.get(object2.toString()));
		}
		return user.getDB().select().toJSONString();
	}
	public String UserSelect() {
		return user.getDB().select().toString();
	}
	public String UserPage(int idx,int pageSize) {
		JSONArray array = user.getDB().page(idx, pageSize);
		@SuppressWarnings("unchecked")
		JSONObject object = new JSONObject(){
			private static final long serialVersionUID = 1L;

			{
				put("totalSize", (int)Math.ceil((double)array.size()/pageSize));
				put("currentPage", idx);
				put("pageSize", pageSize);
				put("data", array);
				
			}
		};
		return object.toString();
	}
	public String UserPageBy(int idx,int pageSize,String userinfo) {
		@SuppressWarnings("unchecked")
		Set<Object> set = JSONHelper.string2json(userinfo).keySet();
		for (Object object2 : set) {
			user.getDB().eq(object2.toString(), JSONHelper.string2json(userinfo).get(object2.toString()));
		}
		JSONArray array = user.getDB().page(idx, pageSize);
		@SuppressWarnings("unchecked")
		JSONObject object = new JSONObject(){
			private static final long serialVersionUID = 1L;
			{
				put("totlsize", (int)Math.ceil((double)array.size()/pageSize));
				put("currentPage", idx);
				put("PageSize", pageSize);
				put("data", user.getDB().page(idx, pageSize).toString());
				
			}
		};
		return object.toString();
	}
	public String UserBatchDelete(String userinfo) {
		String[] arr = userinfo.split(",");
		return resultMessage(user.batch(arr));
	}
	
	private String resultMessage(int no){
		return resultMessage(no, "");
	}
	private String resultMessage(int no,String msg){
		String _msg = "";
		switch(no){
		case 0:
			_msg = msg;
			break;
		case 1:
			_msg = "用户名不正确";
			break;
		case 2:
			_msg = "没有填写Email或者手机号";
			break;
		case 3:
			_msg = "用户名已存在";
			break;
		case 4:
			_msg = "Email格式不正确";
			break;
		case 5:
			_msg = "该Email已被注册";
			break;
		case 6:
			_msg = "该手机号已被注册";
			break;
		case 7:
			_msg = "手机号码格式不正确";
			break;
		case 8:
			_msg = "没有填写Email或者手机号";
			break;
		case 9:
			_msg = "必填项目没有为空";
			break;
		case 10:
			_msg = "密码不合法";
			break;
		case 11:
			_msg = "用户名或者密码错误";
			break;
		default:
			_msg = "操作未知异常";
		}
		return jGrapeFW_Message.netMSG(no,_msg);
	}
}