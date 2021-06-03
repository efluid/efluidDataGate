// Utils

const FORBIDDEN_CHARS = /[`\[\]'"\\\/]/ ;

String.prototype.endsWith = function (suffix) {
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
    $("#successDisplay").text(message);
    $("#successDisplay").append('<button id="closesuccessDisplay" type="button" class="close">\n' +
        '    <span aria-hidden="true">&times;</span>\n' +
        '  </button>')
    $("#successDisplay").fadeIn()
    $("#closesuccessDisplay").click(hideDisplays);
};

const showError = (message) => {
    $("#errorDisplay").text(message).fadeIn();
};

const replaceLoc = (location) => {
    window.location.replace(location);
}

const setInvalid = (inputSelector, message) => {
    showError(message);
    $(inputSelector).addClass("is-invalid").focusin(function () {
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
        if (data.status === 'DIFF_RUNNING' || data.status === 'NOT_LAUNCHED') {
            progressBar.css('width', data.percentDone + '%');
            progressBar.text(data.percentDone + '%');
            setTimeout(checkProgress, 300, service, redirectPath);
            0
        } else {
            // Completed : display completion state
            progressBar.css('width', '100%');
            progressBar.text('100% !!!');
            setTimeout(replaceLoc, 500, redirectPath);
        }
    });
};

// Auto download with auto hide of downloading message
const autoDownloadWithProgress = (uid, name, contentId) => {

    var uri = '/ui/push/' + uid + '/' + name;
    console.info("Auto download " + uri);

    $("#downloadingMessage").show();
    $("#" + contentId).hide();

    // Start a download progress : check if export is generated
    checkDownloadProgress(uid, contentId);

    // To avoid focus change on current, we have no other choice than downloading in JS blob ...
    $.ajax({
        url: uri,
        method: 'GET',
        xhrFields: {
            responseType: 'blob'
        },
        success: function (data) {
            // WARN : this will get the export content into JS blob ...
            var a = document.createElement('a');
            var url = window.URL.createObjectURL(data);
            a.href = url;
            a.download = name;
            document.body.append(a);
            a.click();
            a.remove();
            window.URL.revokeObjectURL(url);
        }
    });
};

// Repeated check on download state
const checkDownloadProgress = (uid, contentId) => {
    var checkUri = '/ui/push/state/' + uid;
    $.get(checkUri, (data, status) => {
        console.info("Check download with data " + data);
        console.info(data);
        // Requires on progress bar in current dom
        if (data) {
            // Completed
            $("#downloadingMessage").hide();
            $("#" + contentId).show();
            0
        } else {
            setTimeout(checkDownloadProgress, 1000, uid, contentId);
        }
    });
}

// Update one gitmoji
const updateGitmoji = (e, gitmojis) => {
    var code = $(e).attr("code");
    var gitmoji = gitmojis.find(g => g.code === code);
    if (gitmoji) {
        $(e).html(gitmoji.emoji);
    }
};

// Common support for gitmojis in a page
const supportGitmojis = () => {
    $.getJSON("/gitmoji/gitmojis.json", function (json) {
        $(".gitmoji").each((index, e) => updateGitmoji(e, json.gitmojis));
    });
};

// Common accessor on element id containing an UUID for reference
const getReferencedUuidInId = (e) => {

    var itemId = e.target.id;
    console.info("processing " + itemId);

    var idParts = itemId.split("_");
    if (idParts.length == 1) {
        return itemId;
    }
    return idParts[idParts.length - 1];
};
