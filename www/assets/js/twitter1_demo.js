function tweetTemplate(currentHandle, tweetTime, tweetText, id, positive, strength, negative) {
    tweetText = tweetText.split(" ");
    var handle = tweetText[0];
    tweetText = tweetText.slice(1).join(" ");
    var tweet = [
        '<div class="dx-content">',
        '<div class="dx-content-item">',
        '<div class="col col3of5">',
        '<div class="ux-content ux-tweet">',
        '<div class="ux-content-item">',
        '<div class="tweet-text">',
        '<span><strong>', handle ,'</strong>', tweetText,
        '</span>',
        '</div>',
        '<div class="tweet-meta">',
        '<div class="tweet-time">', tweetTime,
        '</div>',
        '<div class="tweet-reply">',
        '<a target="_blank" href="http://www.twitter.com/' + currentHandle + '/status/' + id + '">',
        '<i class="fa fa-twitter" aria-hidden="true"></i><span>Reply</span>',
        '</a>',
        '</div>',
        '</div>',
        '</div>',
        '</div>',
        '</div><div class="col col2of5">',
        '<div class="ux-content ux-score">',
        '<div class="ux-content-item">',
        '<div class="tweet-score">',
        '<div class="tweet-progress">',
        '<div class="tweet-progressbar">',
        '<div class="tweet-progressbar-positive ux-positive-bgcolor" role="progressbar" style="width: ' + positive + '%">',
        '</div>',
        '<div class="tweet-progressbar-negative ux-negative-bgcolor" role="progressbar" style="width: ' + negative + '%">',
        '</div>',
        '</div>',
        '</div>',
        '<div class="tweet-analysis">',
        '<div class="col1of3 ui-align-left ">',
        '<div class="tweet-output tweet-positive ux-positive-bgcolor">' + positive + '%',
        '</div>',
        '</div><div class="col1of3 ui-align-center">',
        '<div class="tweet-output tweet-strength ux-strength-bgcolor">', strength,
        '</div>',
        '</div><div class="col1of3 ui-align-right">',
        '<div class="tweet-output tweet-negative ux-negative-bgcolor">' + negative + '%',
        '</div>',
        '</div>',
        '</div>',
        '</div>',
        '</div>',
        '</div>',
        '</div>',
        '</div>',
        '</div>'
    ];
    return tweet.join("\n");
}

function analysisTemplate(noOfPositiveTweet, noOfNegativeTweet) {
    var analysis = [
        '<div class="dx-content">',
        '<div class="dx-content-item">',
        '<div class="col col1of3">',
        '<div class="ux-content">',
        '<div class="ux-header-item ux-positive-color">',
        '<h4>Positive Sentiments</h4>',
        '</div>',
        '<div class="ux-content-item">',
        '<div class="ux-output-item ux-positive-item ux-positive-bgcolor">',
        '<span>' + noOfPositiveTweet + '</span>',
        '</div>',
        '</div>',
        '</div>',
        '</div>',
        '<div class="col col1of3">',
        '<div class="ux-content">',
        '<div class="ux-header-item ux-negative-color">',
        '<h4>Negative Sentiments</h4>',
        '</div>',
        '<div class="ux-content-item">',
        '<div class="ux-output-item ux-negative-item ux-negative-bgcolor">',
        '<span>' + noOfNegativeTweet + '</span>',
        '</div>',
        '</div>',
        '</div>',
        '</div>',
        '<div class="col col1of3">',
        '<div class="ux-content">',
        '<div class="ux-header-item">',
        '<h4>Sentiment Analysis</h4>',
        '</div>',
        '<div class="ux-content-item">',
        '<div class="chart"></div>',
        '</div>',
        '</div>',
        '</div>',
        '</div>',
        '</div>'
    ];
    return analysis.join("\n");
}

var clientData;
var firstEntry = 0;
var currentHandle;
var timeMS = 180000;

$('.twitter-handle').on('typeahead:select', function(e, selectedHandle) {
    firstEntry = 0;
    currentHandle = selectedHandle;
    getResults(selectedHandle);
    setTimeout(newResults, timeMS);
});

// $('.twitter-handle').on('typeahead:change', function (e, selectedHandle) {
//   firstEntry = 0;
//   currentHandle = selectedHandle;
//   getResults(selectedHandle);
//   setTimeout( newResults, timeMS );
// });

function getResults(handle) {
    $.ajax({
        url: '/app/reports/' + handle + '.json',
        method: 'GET',
        success: displayTweets,
        error: function(xhr, status, error) {
            emptyPrevTweets();
            $('.twitter-handle-error').text('@' + handle + ' does not exist!');
            $('.twitter-handle-error').show();
            $('.twitter-handle-success').hide();
        },
        dataType: 'json'
    });
}

function changeValue(val) {
    displayTweets(clientData, val);
}

function emptyPrevTweets() {
    $(".analysis").hide();
    $('.ax-analysis .wrap').empty();
    $(".ax-tweets .wrap").empty();
}

function displayChart(dataset) {
    $('.chart').empty();
    var width = 100,
        height = 100,
        radius = Math.min(width, height) / 2;

    var color = d3.scale.ordinal().range(["#54ad02", "#e60010"]);

    var arc = d3.svg.arc()
        .outerRadius(radius)
        .innerRadius(radius - 10);

    var pie = d3.layout.pie()
        .value(function(d) {
            return d.count;
        })
        .sort(null);

    var svg = d3.selectAll('.chart')
        .append('svg')
        .attr('width', width)
        .attr('height', height)
        .append('g')
        .attr('transform', 'translate(' + (width / 2) + ',' + (height / 2) + ')');

    var path = svg.selectAll('path')
        .data(pie(dataset))
        .enter()
        .append('path')
        .attr('d', arc)
        .attr('fill', function(d, i) {
            return color(d.data.label);
        });
}

function displayTweets(data, val) {
    emptyPrevTweets();

    if (firstEntry == 0) {
        clientData = data;
    }

    firstEntry++;

    if (val == 'success') {
        val = 0;
    }

    var selectedTweets = [];
    var scoreList = [];
    var posList = [];
    var negList = [];
    var analysedTweets = data['results'];
    if (analysedTweets.length == 0) {
        $('.twitter-handle-error').text('Unable to fetch tweets at the current moment. Please try after sometime.');
        $('.twitter-handle-error').show();
        $('.twitter-handle-success').hide();
        emptyPrevTweets();
    } else {
        minScr = Number.MAX_SAFE_INTEGER;
        maxScr = 0;
        tweetCount = 0;
        posCount = 0;
        negCount = 0;

        for (var i = 0; i < analysedTweets.length; i++) {
            var tweet = analysedTweets[i];

            if (tweet.score == 0) {
                continue;
            }

            if (tweet.score > 0) {
                scoreList.push(tweet.score);
            }

            if (tweet.score > maxScr) {
                maxScr = Math.round(tweet.score * 100) / 100;
            }

            if (tweet.score < minScr) {
                minScr = Math.round(tweet.score * 100) / 100;
            }

            if (tweet.score > val) {
                if (tweet.pos_per - tweet.neg_per > 20) {
                    tweetCount++;
                    posCount++;
                    selectedTweets.push(tweet);
                    posList.push(tweet.pos_per / 100);
                } else if (tweet.neg_per - tweet.pos_per > 20) {
                    tweetCount++;
                    negCount++;
                    selectedTweets.push(tweet);
                    negList.push(tweet.neg_per / 100);
                }
            }
        }

        if (val == 0) {
            posCount = 0;
            negCount = 0;
            var median = math.median(scoreList);
            val = Math.round(median * 100) / 100;

            tweetCount = 0;
            for (var i = 0; i < analysedTweets.length; i++) {
                var tweet = analysedTweets[i];
                if (tweet.score > val) {
                    if (tweet.pos_per - tweet.neg_per > 20) {
                        tweetCount++;
                        posCount++;
                        selectedTweets.push(tweet);
                        posList.push(tweet.pos_per / 100);
                    } else if (tweet.neg_per - tweet.pos_per > 20) {
                        tweetCount++;
                        negCount++;
                        selectedTweets.push(tweet);
                        negList.push(tweet.neg_per / 100);
                    }
                }
            }
        }

        function sum(a, b) {
            return a + b;
        }
        var pieData = [{
            label: 'positive',
            count: (posList.reduce(sum, 0) / tweetCount)
        }, {
            label: 'Negative',
            count: (negList.reduce(sum, 0) / tweetCount)
        }, ];

        function parseTweetTime(created_at) {
            var temp = created_at.split(" ");
            var dateTime = temp[3];
            temp[3] = dateTime.substr(0, 2) + ":" + dateTime.substr(2, 2) + ":" + dateTime.substr(4, 2);
            return moment(temp.join(" "), 'dd MMM DD HH:mm:ss ZZ YYYY', 'en').utc().format('DD-MMM-YY[\n]HH:mm A z');
        }
        var slider = ['<div class="dx-content">',
            '<div class="dx-content-item">',
            '<div class="col col3of5">',
            '</div>',
            '<div class="col col2of5">',
            '<div class="ux-content ux-range">',
            '<form>',
            '<input value="' + val + '" onchange="changeValue(this.value)" step="0.01" min="' + minScr + '" max="' + maxScr + '" type="range">',
            '</form>',
            '</div>',
            '</div>',
            '</div>',
            '</div>'
        ];

        $('.twitter-handle-error').hide();
        $('.twitter-handle-success').hide();
        $('.analysis .ux-strength-item span').text(minScr + ' - ' + maxScr);
        $('.analysis .ux-strength-item-text span').text(tweetCount + ' tweets > strength of ' + val);
        $('.analysis').show();
        if (tweetCount) {
            $('.ax-analysis .wrap').append(analysisTemplate(posCount, negCount));
            displayChart(pieData);
        }

        $(".ax-tweets .wrap").append(slider.join("\n"));

        for (var i = 0; i < selectedTweets.length; i++) {
            var tweet = analysedTweets[i];
            if (tweet.score > val) {
                $('.ax-tweets .wrap').append(tweetTemplate(currentHandle, parseTweetTime(tweet.created_at), tweet.tweet, tweet.tweet_id, tweet.pos_per, Math.round(tweet.score * 100) / 100, tweet.neg_per));
            }
        }
    }
}

function newResults() {
    firstEntry = 0;
    getResults(currentHandle);
    setTimeout(newResults, timeMS);
}

function setDefaultHandle(handle) {
    $('.twitter-handle').typeahead('val', handle, getResults(handle));
    currentHandle = handle;
    setTimeout(newResults, timeMS);
}

function initTypeahead(list) {
    var substringMatcher = function(strs) {
        return function findMatches(q, cb) {
            var matches, substringRegex;
            matches = [];
            substrRegex = new RegExp(q, 'i');
            $.each(strs, function(i, str) {
                if (substrRegex.test(str)) {
                    matches.push(str);
                }
            });
            cb(matches);
        };
    };

    $('.typeahead').typeahead('destroy');
    $('.typeahead').typeahead({
        minLength: 0,
        highlight: true
    }, {
        source: substringMatcher(list),
        limit: 15
    })
}

$.ajax({
    url: '/app/twitter-handle',
    method: 'GET',
    success: function(data) {
        var handles = JSON.parse(data);
        initTypeahead(handles);
        setDefaultHandle(handles[0]);
    }
})

$(".addhandle").click(function(event) {
    event.preventDefault();
    var customHandle = $("#customhandle").val();
    if (customHandle)
        $.ajax({
            url: '/app/twitter-handle',
            method: 'POST',
            data: {
                handle: customHandle
            },
            success: function(data) {
                initTypeahead(JSON.parse(data));
                $("#customhandle").val('');
                $('.twitter-handle-success')
                    .text('Twitter handle: ' + customHandle + ' added. Please check after 15 minutes for results.');
                $('.twitter-handle-success').show();
                $('.twitter-handle-error').hide();
            },
            error: function(xhr, error, status) {
                $('.twitter-handle-error').text('Handle cannot be added');
                $('.twitter-handle-success').hide();
                $('.twitter-handle-error').show();
            }
        })
    else {
        $('.twitter-handle-error').text('Give a valid handle!');
        $('.twitter-handle-success').hide();
        $('.twitter-handle-error').show();
    }
    emptyPrevTweets();
})
