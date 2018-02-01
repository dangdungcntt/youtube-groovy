class Request {

    private static final String USER_AGENT = "Mozilla/5.0"

    static def get(options) {
        def url, params, query = ""

        if (options in String) {
            url = options
        } else {
            url = options.url
            params = options.params

            for (String key : params.keySet()) {
                query += "${key}=${params.get(key)}&"
            }
        }

        URL obj = new URL(url + "?" + query)
        HttpURLConnection con = (HttpURLConnection) obj.openConnection()

        con.setRequestMethod("GET")
        con.setRequestProperty("User-Agent", USER_AGENT)
        con.setRequestProperty("Accept-Charset", "UTF-8")

        return con.getInputStream().getText("UTF-8")
    }
}
