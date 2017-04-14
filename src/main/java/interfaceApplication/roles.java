package interfaceApplication;

import java.util.HashMap;

import org.apache.commons.lang3.StringEscapeUtils;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;

import esayhelper.JSONHelper;
import model.RolesModel;

@SuppressWarnings("unchecked")
public class roles {
	private RolesModel rolesModel = new RolesModel();
	private HashMap<String, Object> defcol = new HashMap<>();
	private JSONObject _obj = new JSONObject();

	public roles() {
		defcol.put("ugid", rolesModel.getID());
		defcol.put("ownid", 0);
		defcol.put("sort", 0);
		defcol.put("fatherid", 0);
		defcol.put("wbid", 0);
	}

	public String RoleInsert(String roleInfo) {
		JSONObject object = rolesModel.addMap(defcol, JSONHelper.string2json(roleInfo));
		return rolesModel.resultMessage(rolesModel.insert(object), "新增角色成功");
	}

	public String RoleUpdate(String id, String roleInfo) {
		return rolesModel.resultMessage(rolesModel.update(id, JSONHelper.string2json(
				roleInfo)), "修改角色成功");
	}

	/**
	 * 批量修改 修改排序值和层级关系
	 * 
	 * @param arraystring
	 * @return
	 */
	public String RoleUpdateBatch(String arraystring) {
		int code = 0;
		JSONArray array = (JSONArray) JSONValue.parse(arraystring);
		for (int i = 0; i < array.size(); i++) {
			if (code != 0) {
				return rolesModel.resultMessage(2, "");
			}
			JSONObject object = (JSONObject) array.get(i);
			if (object.containsKey("fatherid")) {
				code = rolesModel.setFatherId(object.get("_id").toString(), object.get("fatherid")
						.toString());
			} else {
				code = rolesModel.setsort(object.get("_id").toString(), Integer.parseInt(object
						.get("sort").toString()));
			}
		}
		return rolesModel.resultMessage(code, "设置顺序或层级成功");
	}
	// public String RoleSelect() {
	// return rolesModel.resultMessage(0,rolesModel.select().toString());
	// }

	public String RoleSearch(String roleInfo) {
		_obj.put("records", rolesModel.select(JSONHelper.string2json(roleInfo)));
		return StringEscapeUtils.unescapeJava(rolesModel.resultMessage(0, _obj.toString()));
	}

	public String RoleDelete(String id) {
		return rolesModel.resultMessage(rolesModel.delete(id), "角色删除成功");
	}

	public String RoleBatchDelete(String ids) {
		return rolesModel.resultMessage(rolesModel.delete(ids.split(",")), "批量删除成功");
	}

	public String RolePage(int idx, int pageSize) {
		_obj.put("records", rolesModel.page(idx, pageSize));
		return StringEscapeUtils.unescapeJava(rolesModel.resultMessage(0, _obj.toString()));
	}

	public String RolePageBy(int idx, int pageSize, String roleInfo) {
		_obj.put("records", rolesModel.page(idx, pageSize, JSONHelper.string2json(roleInfo)));
		return StringEscapeUtils.unescapeJava(rolesModel.resultMessage(0, _obj.toString()));
	}
	 public String RoleSetSort(String id,long sort) {
	 return rolesModel.resultMessage(rolesModel.setsort(id, sort), "设置排序值成功");
	 }
	public String RoleSetFatherId(String id, String fatherid) {
		return rolesModel.resultMessage(rolesModel.setFatherId(id, fatherid), "上级用户组设置成功");
	}
}
