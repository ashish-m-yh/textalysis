function tweetTemplate(currentHandle, time, city, tweet, id, positive, strength, negative) {
    var tweet = [
        '<div class="table-row">',
        '<div class="table-col">',
        '<div class="tweet-meta">',
        '<div class="tweet-time">', time, '</div>',
        '<div class="tweet-location"></div>',
        '<div class="tweet-reply">',
        '<a target="_blank" href="http://www.twitter.com/' + currentHandle + '/status/' + id + '">',
        '<button type="button">Reply</button>',
        '</a>',
        '</div>',
        '</div>',
        '</div>',
        '<div class="table-col">',
        '<div class="tweet-text">', tweet, '</div>',
        '</div>',
        '<div class="table-col">',
        '<div class="score">',
        '<div class="tweet-progress-bar">',
        '<div class="progress">',
        '<div class="progress-bar ux-bgcolor-positive" role="progressbar" style="width: ' + positive + '%">',
        '</div>',
        '<div class="progress-bar ux-bgcolor-negative" role="progressbar" style="width: ' + negative + '%">',
        '</div>',
        '</div>',
        '</div>',
        '<div class="tweet-analysis">',
        '<div class="tweet-output tweet-positive ux-bgcolor-positive">', positive + "%", '</div>',
        '<div class="tweet-output tweet-strength ux-bgcolor-strength">', strength, '</div>',
        '<div class="tweet-output tweet-negative ux-bgcolor-negative">', negative + "%", '</div>',
        '</div>',
        '</div>',
        '</div>',
        '</div>' ];
    return tweet.join("\n");
}

function analysisTemplate(sentimentRange, analysisText, noOfPositiveTweet, noOfNegativeTweet, value, minScr, maxScr) {
    var analysis = [
        '<div class="spacer"></div>',
        '<div class="analysis">',
        '<section id="sx-site" class="sx-site">',
        '<article class="ax-content">',
        '<article class="ax-content-item">',
        '<section class="wrap">',
        '<div class="dx-content">',
        '<div class="dx-content-item">',
        '<div class="col col1of3">',
        '<div class="ux-content">',
        '<div class="ux-content-item">',
        '<div class="ux-item-output ux-item-strength ux-bgcolor-strength">',
        '<span>' + sentimentRange + '</span>',
        '</div>',
        '<div class="ux-item-output-text ux-item-strength-text ux-color-strength">',
        '<span>Sentiment Strength</span>',
        '</div>',
        '</div>',
        '<div class="ux-content-item">',
        '<div class="ux-item-report ux-color-strength">',
        '<h5>' + analysisText + '</h5>',
        '<p>Importance of a tweet</p>',
        '</div>',
        '</div>',
        '</div>',
        '</div>',
        '<div class="col col1of3">',
        '<div class="ux-content">',
        '<div class="ux-content-item">',
        '<div class="ux-item-output ux-item-positive ux-bgcolor-positive">',
        '<span>' + noOfPositiveTweet + '</span>',
        '</div>',
        '<div class="ux-item-output-text ux-item-positive-text ux-color-strength">',
        '<span>Positive Sentiments</span>',
        '</div>',
        '</div>',
        '<div class="ux-content-item">',
        '<div class="ux-item-output ux-item-negative ux-bgcolor-negative">',
        '<span>' + noOfNegativeTweet + '</span>',
        '</div>',
        '<div class="ux-item-output-text ux-item-negative-text ux-color-strength">',
        '<span>Negative Sentiments</span>',
        '</div>',
        '</div>',
        '</div>',
        '</div>',
        '<div class="col col1of3">',
        '<div class="ux-content">',
        '<div class="ux-content-item">',
        '<div class="chart"></div>',
        '</div>',
        '</div>',
        '</div>',
        '</div>',
        '</div>',
        '</section>',
        '</article>',
        '</article>',
        '</section>',
        '</div>',
        '<div class="row slider">',
        '<form>',
        '<input type="range" value="' + value + '" onchange="changeValue(this.value)" step="0.01" min="'+ minScr +'" max="'+ maxScr +'">',
        '</form>',
        '</div>',
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
            $('.twitter-handle-error').text('@' + handle + ' does not exist!');
            $('.twitter-handle-error').show();
            $('.twitter-handle-success').hide();
            $('.report').hide();
        },
        dataType: 'json'
    });
}

function changeValue(val) {
    displayTweets(clientData, val);
}

function displayChart(dataset) {
    $('.chart').empty();
    var width = 150,
        height = 150,
        radius = Math.min(width, height) / 2;

    var color = d3.scale.ordinal().range(["#54ad02", "#e60010"]);

    var arc = d3.svg.arc()
        .outerRadius(radius)
        .innerRadius(radius - 15);

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
    $(".report").empty();

    if (firstEntry == 0) {
        clientData = data;
    }

    firstEntry++;

    if (val == 'success') {
        val = 0;
    }

    var scoreList = [];
    var posList = [];
    var negList = [];
    var analysedTweets = data['results'];
    if (analysedTweets.length == 0) {
        $('.twitter-handle-error').text('Unable to fetch tweets at the current moment. Please try after sometime.');
        $('.twitter-handle-error').show();
        $('.twitter-handle-success').hide();
        $('.report').hide();
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
                tweetCount++;

                if (tweet.pos_per - tweet.neg_per > 20) {
                    posCount++;
                    posList.push(tweet.pos_per / 100);
                } else if (tweet.neg_per - tweet.pos_per > 20) {
                    negCount++;
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
                    tweetCount++;

                    if (tweet.pos_per - tweet.neg_per > 20) {
                        posCount++;
                        posList.push(tweet.pos_per / 100);
                    } else if (tweet.neg_per - tweet.pos_per > 20) {
                        negCount++;
                        negList.push(tweet.neg_per / 100);
                    }
                }
            }
        }

        function sum(a, b) {
            return a + b;
        }
        var pieData = [
            { label: 'positive', count: (posList.reduce(sum, 0) / tweetCount) },
            { label: 'Negative', count: (negList.reduce(sum, 0) / tweetCount) },
        ];

        function parseTweetTime(created_at) {
            var temp = created_at.split(" ");
            var dateTime = temp[3];
            temp[3] = dateTime.substr(0, 2) + ":" + dateTime.substr(2, 2) + ":" + dateTime.substr(4, 2);
            return moment(temp.join(" "), 'dd MMM DD HH:mm:ss ZZ YYYY', 'en').utc().format('DD-MMM-YY[\n]HH:mm A z');
        }

        $('.report').append(analysisTemplate(minScr + ' - ' + maxScr, tweetCount + " tweets > strength of " + val, posCount, negCount, val, minScr, maxScr));
        displayChart(pieData);
        $('.report').append('<div class="table"></div>');

        for (var i = 0; i < analysedTweets.length; i++) {
            var tweet = analysedTweets[i];
            if (tweet.score > val)
                {
                    $('.table').append(tweetTemplate(currentHandle, parseTweetTime(tweet.created_at), 'dummy city', tweet.tweet, tweet.tweet_id, tweet.pos_per, Math.round(tweet.score * 100) / 100, tweet.neg_per));
                }
        }
        $('.report').show();
        $('.twitter-handle-error').hide();
        $('.twitter-handle-success').hide();
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
            data: { handle: customHandle },
            success: function(data) {
                initTypeahead(JSON.parse(data));
                $("#customhandle").val('');
                $('.twitter-handle-success')
                    .text('Twitter handle: ' + customHandle + ' added. Please check after 15 minutes for results.');
                $('.twitter-handle-success').show();
                $('.twitter-handle-error').hide();
                $('.report').hide();
            },
            error: function(xhr, error, status) {
                $('.twitter-handle-error').text('Handle cannot be added');
                $('.twitter-handle-success').hide();
                $('.twitter-handle-error').show();
                $('.report').hide();
            }
        })
    else {
        $('.twitter-handle-error').text('Give a valid handle!');
        $('.twitter-handle-success').hide();
        $('.twitter-handle-error').show();
        $('.report').hide();
    }
})