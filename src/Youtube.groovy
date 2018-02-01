class Youtube {

    static def getInfo(String url) {
        def video_id = getVideoId(url)

        if (!video_id) return false

        def options = [
                url   : 'http://www.youtube.com/get_video_info',
                method: "GET",
                params: [
                        video_id: video_id
                ]
        ]

        def response = Request.get(options)

        if (response.indexOf('errorcode') > -1) return false

        def video_info = response

        def data = parseStr(video_info)

        def url_encoded_fmt_stream_map = data.url_encoded_fmt_stream_map
        def title = data.title
        def thumbnail_url = data.thumbnail_url
        def view_count = data.view_count
        def length_seconds = data.length_seconds
        def allow_embed = data.allow_embed
        def author = data.author

        def streamsMap = url_encoded_fmt_stream_map.toString().split(",")

        def formats = []

        for (String stream : streamsMap) {
            formats.push(parseStr(stream))
        }

        return [
                "video_id"      : video_id,
                "title"         : title,
                "thumbnail_url" : thumbnail_url,
                "view_count"    : view_count,
                "length_seconds": length_seconds,
                "allow_embed"   : allow_embed,
                "author"        : author,
                "formats"       : formats
        ]
    }

    static def getVideoId(String url) {

        if (MyRegex.test(/youtu\.?be/, url)) {

            // Look first for known patterns
            def patterns = [
                    /youtu\.be\/([^#\&\?]{11})/,  // youtu.be/<id>
                    /\?v=([^#\&\?]{11})/,         // ?v=<id>
                    /\&v=([^#\&\?]{11})/,         // &v=<id>
                    /embed\/([^#\&\?]{11})/,      // embed/<id>
                    /\/v\/([^#\&\?]{11})/         // /v/<id>
            ]

            // If any pattern matches, return the ID
            for (def i = 0; i < patterns.size(); ++i) {
                if (MyRegex.test(patterns[i], url)) {
                    return MyRegex.exec(patterns[i], url)[0]
                }
            }
            // If that fails, break it apart by certain characters and look
            // for the 11 character key
            def tokens = url.split(/[\/\&\?=#\.\s]/)
            for (def i = 0; i < tokens.size(); ++i) {
                if (MyRegex.test(/^[^#\&\?]{11}$/, tokens[i])) {
                    return tokens[i]
                }
            }
        }

        return null
    }

    static def parseStr(String str) {
        String[] strArr = str.replace(/^&/, '').replace(/&$/, '').split('&')
        int sal = strArr.length

        def array = new LinkedHashMap()

        def obj = new LinkedHashMap()

        for (int i = 0; i < sal; i++) {
            String[] tmp = strArr[i].split('=')
            String key = _fixStr(tmp[0])
            String value = (tmp.size() < 2) ? '' : _fixStr(tmp[1])

            key = key.trim()

            if (key.indexOf($/\x00/$) > -1) {
                key = key.substring(0, key.indexOf($/\x00/$))
            }

            if (key && key.charAt(0) != '[') {
                ArrayList<String> keys = []
                int postLeftBracketPos = 0
                for (int j = 0; j < key.length(); j++) {
                    if (key.charAt(j) == '[' && !postLeftBracketPos) {
                        postLeftBracketPos = j + 1
                    } else if (key.charAt(j) == ']') {
                        if (postLeftBracketPos) {
                            if (!keys.size()) {
                                keys.push(key.substring(0, postLeftBracketPos - 1))
                            }
                            keys.push(key.substring(postLeftBracketPos, j))
                            postLeftBracketPos = 0
                            if (j + 1 < key.length() && key.charAt(j + 1) != '[') {
                                break
                            }
                        }
                    }

                }

                if (!keys.size()) {
                    keys.push(key)
                }

                for (int j = 0; j < keys[0].length(); j++) {
                    char chr = keys[0].charAt(j)
                    if (chr == ' ' || chr == '.' || chr == '[') {
                        keys.set(0, keys.get(0).substring(0, j) + '_' + keys.get(0).substring(j + 1))
                    }
                    if (chr == '[') {
                        break
                    }
                }

                int keysLen = keys.size()

                obj = array
                def lastObj
                for (int j = 0; j < keysLen; j++) {
                    key = keys[j].replace(/^['"]/, '').replace(/['"]$/, '')
                    lastObj = obj
                    if ((key != '' && key != ' ') || j == 0) {
                        if (!obj.get(key)) {
                            obj.put(key, new LinkedHashMap())
                        }
                        obj = obj.get(key)
                    } else {
                        // To insert new dimension
                        int ct = -1
                        for (String p in obj.keySet()) {
                            int index = Integer.parseInt(p)
                            if (index > ct && MyRegex.test(/^\d+$/, p)) {
                                ct = index
                            }
                        }
                        key = ct + 1
                    }
                }

                lastObj.put(key, value)
            }
        }

        return array
    }

    static _fixStr(String str) {
        return URLDecoder.decode(str.replace(/\+/, '%20'), "UTF-8")
    }
}