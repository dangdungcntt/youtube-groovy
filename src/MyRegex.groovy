import java.util.regex.Matcher
import java.util.regex.Pattern

class MyRegex {

    static boolean test(regex, str) {
        return Pattern.compile(regex).matcher(str).size() > 0
    }

    static def exec(regex, str) {

        Matcher m = Pattern.compile(regex).matcher(str)

        ArrayList res = new ArrayList()

        while (m.find()) {
            def count = m.groupCount()
            if (!count) {
                res.push(m.group())
                continue
            }
            for (def i = 1; i <= count; i++) {
                res.push(m.group(i))
            }
        }

        return res
    }
}