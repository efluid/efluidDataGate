// Utils

String.prototype.endsWith = function(suffix) {
    return this.indexOf(suffix, this.length - suffix.length) !== -1;
};

const hideDisplays = () => {
	$("#successDisplay").hide();
	$("#errorDisplay").hide();
};

const showSuccessHtml = (html) => {
    $("#successDisplay").html(html).fadeIn();
};

const showSuccess = (message) => {
    $("#successDisplay").text(message).fadeIn();
};

const showError = (message) => {
    $("#errorDisplay").text(message).fadeIn();
};

const replaceLoc = (location) => {
    window.location.replace(location);
}

const setInvalid = (inputSelector, message) => {
    showError(message);
    $(inputSelector).addClass("is-invalid").focusin(function(){
        $("#errorDisplay").hide();
        $(this).removeClass("is-invalid");
    });
};

// Repeated check on status using basic rest service
const checkProgress = (service, redirectPath) => {
    console.info("start a new check");
    $.get(service, (data, status) => {
        console.info("Check with data " + data);
        // Requires on progress bar in current dom
        var progressBar = $("#progressBar");
        if(data.status === 'DIFF_RUNNING' || data.status === 'NOT_LAUNCHED'){
			progressBar.css('width', data.percentDone + '%');
			progressBar.text(data.percentDone + '%');
			setTimeout(checkProgress, 300, service, redirectPath);
0		} else {
		    // Completed : display completion state
			progressBar.css('width', '100%');
			progressBar.text('100% !!!');
			setTimeout(replaceLoc, 500, redirectPath);
		}
	});
};

// Update one gitmoji
const updateGitmoji = (e, gitmojis) => {
	var code = $(e).attr("code");
	var gitmoji = gitmojis.find(g => g.code === code);
	if(gitmoji){
		$(e).html(gitmoji.entity);
	}
};

// Common support for gitmojis in a page
const supportGitmojis = () => {
    $.getJSON("/gitmoji/gitmojis.json", function(json) {
        $(".gitmoji").each((index, e) => updateGitmoji(e, json.gitmojis));
    });
}