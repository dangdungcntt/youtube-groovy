import static groovy.json.JsonOutput.*

print prettyPrint(toJson(Youtube.getInfo("https://www.youtube.com/embed/ceszBfg01WM")))