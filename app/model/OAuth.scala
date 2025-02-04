package model

object OAuth {
  def makeUrl(endpoint: String, qs: (String, String)*): String = {
    import java.net.URLEncoder.{encode => enc}
    val params = for ((n, v) <- qs) yield s"""${enc(n, "utf-8")}=${enc(v, "utf-8")}"""
    endpoint + params.mkString("?", "&", "")
  }

}
