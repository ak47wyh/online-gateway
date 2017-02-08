var Share = {};

/** 得到最顶层的window对象 */
Share.getWin = function() {
	var win = window;
	while (win != win.parent) {
		win = win.parent;
	}
	return win;
};

Share.ExportByExcel = function(url) {
	var appWindow = Share.getWin();
	appWindow.open(url);
	appWindow.focus();
};