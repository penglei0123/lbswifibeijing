/**
 * 前端公用JS脚本
 * @author hzy
 */
function isEmail(str){
	var reg = /^([a-zA-Z0-9_-])+@([a-zA-Z0-9_-])+(.[a-zA-Z0-9_-])+/;
	return reg.test(str);
}

function isPassword(str){
	var reg = /^(?![0-9]+$)(?![a-zA-Z]+$)[0-9A-Za-z]{6,15}/;
	return reg.test(str);
}

function isNumber(str){
	var reg = /^[0-9]*$/;
	return reg.test(str);
}