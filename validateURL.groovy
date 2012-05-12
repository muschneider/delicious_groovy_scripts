import javax.net.ssl.HttpsURLConnection
import java.net.*
import java.io.*

def userName = ""
def passWord = ""

if (this.args.length == 2) {
	userName = this.args[0]
	passWord = this.args[1]
} else {
    println "use: groovy validateURL <delicious_user_name> <delicious_password> "
    return -1
}

Authenticator.setDefault(new Authenticator() {
  PasswordAuthentication getPasswordAuthentication() {
    return new PasswordAuthentication(userName ,passWord.toCharArray())
  }
});


def deliciousPosts = {
    def postsList = []
    def url = new URL("https://${userName}:${passWord}@api.del.icio.us/v1/posts/all")
    def posts = new XmlParser().parseText(url.text)
    posts.each{ pAttr ->
        def description = pAttr.attribute("description")
        def href = pAttr.attribute("href")
        def tags = pAttr.attribute("tag")

        def post = [:]
        post.href = href
        post.description = description
        post.tags = tags
        postsList.add(post)
    }
    postsList
}

def validateUrl(href) {
    def code = -1
    def url = href.toURL()
    try {
        def conn = url.openConnection();
        conn.connect();
        if ( conn instanceof HttpURLConnection) {
            def httpConnection = (HttpURLConnection) conn;
            code = httpConnection.getResponseCode();
        }
    } catch (MalformedURLException e) {
        code = -1 
    } catch (IOException e) {
        code = -1 
    }
    code
}


deliciousPosts().each {
    def connCode = validateUrl(it.href)
    if (connCode != 200) {
        println "Code        : " + (connCode < 0 ? "site error":connCode)
        println "url         : " + it.href
        println "description : " + it.description
        println "tags        : " + it.tags
        println "\n"
    }
}
