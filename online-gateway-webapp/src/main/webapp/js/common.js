var APP_NAME ='/'+(document.location+'').replace(/http:\/\/[^\/]+\/([^\/]+)\/?.*/ig, '$1');
$(document).ready(function(){
	initCompanyAccSelect();
});

function initCompanyAccSelect(){
	var companyAccSelect = $('#companyAccSelect');
	var queryString = window.location.search;
	var selectName = $.trim(companyAccSelect.attr('name'));
	var selectValue = '';
	if(selectName != ''){
		var regExp = new RegExp('.*'+selectName + "=([^&/]+).*", "i");
		selectValue = queryString.replace(regExp, '$1');
	}
	if('' == $.trim(companyAccSelect.val())){
		$.get(APP_NAME+"/manage/account/list.htm?type=json", function(result){
			companyAccSelect.empty();
			companyAccSelect.append('<option value="" selected>--选择公司账号--</option>');
			for(var i=0; i<result.length; i++){
				companyAccSelect.append('<option value="'+result[i].accNo+'" bankName="'+result[i].bankName+'" '+(result[i].accNo == selectValue ? 'selected="selected"' : '')+'>'+result[i].accNo+'&nbsp;&nbsp;'+result[i].accName
						+'&nbsp;&nbsp;'+result[i].bankFullName+'</option>');
			}
		});
	}
}

function pager(pagerId, pageBean, reqFun){
	var pagerSpan = $('#' + pagerId);
	pagerSpan.empty('');
	if(pageBean.pageNo > 1) 
		pagerSpan.append('<a href="#" style="color: white; font-size: 12ox" onclick="'+reqFun+'('+(pageBean.pageNo-1)+'); return false;">上一页</a>');
	if(pageBean.pageNo == 1)
		pagerSpan.append('上一页');
	pagerSpan.append('&nbsp;&nbsp;');
	if(pageBean.pageNo < pageBean.totalPages)
		pagerSpan.append('<a href="#" style="color: white; font-size: 12ox" onclick="'+reqFun+'('+(pageBean.pageNo+1)+'); return false;">下一页</a>');
	if(pageBean.pageNo == pageBean.totalPages)
		pagerSpan.append('下一页');
	pagerSpan.append('<span style="color: white;">(当前页='+pageBean.pageNo+', 总页数='+pageBean.totalPages+', 总数='+pageBean.totalCount+')</span>');
}


;(function($) {
	$.extend({
		/**
		 * 调用方法： var timerArr = $.WindowMsg.show();
		 *			$.WindowMsg.clear();
		 */
		WindowMsg : {
			msgInfo:{},
			show : function(type, msg) {	//有新消息时在title处闪烁提示
				var step=0, _title = this.msgInfo.title || window.top.document.title;
				var blankMsg = '';
				for(var i=0; i<type.length; i++){
					blankMsg +='　';
				}
				var timer = setInterval(function() {
					switch (step) {
					case 1:
						window.top.document.title='【'+blankMsg+'】' + msg;	
						break;
					case 2:
						window.top.document.title='【'+type+'】' + msg;
						break;
					default:
						break;
					}
					if(++step > 2)
						step = 1;
				}, 300);
 
				this.msgInfo.timer = timer;
				this.msgInfo.title = _title;
			},
 
			/**
			 * @param timerArr[0], timer标记
			 * @param timerArr[1], 初始的title文本内容
			 */
			clear : function() {	//去除闪烁提示，恢复初始title文本
				if(this.msgInfo.timer) {
					clearInterval(this.msgInfo.timer);	
					window.top.document.title = this.msgInfo.title;
				};
			}
		}
	});
})(jQuery);