package me.roybailey.research.kotlin.htmlparsing

import org.eclipse.jetty.http.HttpHeader
import org.jsoup.Connection
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.jsoup.nodes.Element
import org.jsoup.nodes.FormElement
import org.jsoup.select.Elements
import org.junit.jupiter.api.Test


class JsoupTest {

    @Test
    fun testJsoupParsing() {
        val doc: Document = Jsoup.connect("https://en.wikipedia.org/").get()
        println(doc.title())
        val newsHeadlines: Elements = doc.select("#mp-itn b a")
        for (headline in newsHeadlines) {
            println(String.format("%s\n\t%s", headline.attr("title"), headline.absUrl("href")))
        }
    }


    @Test
    fun testAutoTraderSearch() {
        val url = "https://www.autotrader.co.uk/car-search?postcode=co63pf"
        val response = Jsoup.connect(url)
            .timeout(30000)
            .method(Connection.Method.GET)
            .execute();
        val document = response.parse()
        for (element in document.select("ul.search-page__products > li > article.product-card > div > div.product-card-content")) {
            println("----------")
            println(element.text())
        }
    }

    @Test
    fun testJsoupShareDealingFormSubmission() {
        val url = "https://www.markets.iweb-sharedealing.co.uk/shares-centre/"
        val response = Jsoup.connect(url)
            .timeout(30000)
            .method(Connection.Method.GET)
            .execute()

        val document = response.parse()
        val searchForm = document.getElementsByAttributeValue("action", "/investments-search/").first() as FormElement

        val searchInput = searchForm.select("#query").first() as Element
        searchInput.text("a")

        val searchResponse = searchForm.submit().cookies(response.cookies())
            .timeout(30000)
            .method(Connection.Method.GET)
            .execute()
        val searchResults = searchResponse.parse()
        for (entityNumber in searchResults.select("div.container-table-search > table > tr")) {
            println("----------")
            println(entityNumber.text())
        }
    }

    data class WebSharePrice(
        val name: String,
        val price: String
    )

    @Test
    fun testJsoupShareDealing() {
        var page = 1
        do {
            val url =
                "https://www.markets.iweb-sharedealing.co.uk/investments-search/result/?term=a&asset_type=Equity&size=50"
            val response = Jsoup.connect(url)
                .timeout(30000)
                .method(Connection.Method.POST)
                .header(HttpHeader.CONTENT_TYPE.asString(), "application/x-www-form-urlencoded")
                .requestBody("page=$page")
                .execute()
            val document = response.parse()
            for (row in document.select("table tbody tr")) {
                val share = WebSharePrice(
                    row.selectFirst("td[data-col='name']")!!.text(),
                    row.selectFirst("td[data-col='price']")!!.text()
                )
                println(share)
            }
            println("----------")
            ++page
        } while (response.statusCode() == 200 && page <= 10)
    }

    data class Salary(
        val takehome: String
    )

    @Test
    fun testJsoupSalary() {
        var salary = 50000
        var maxSalary = 50000
        do {
            val url = "https://www.thesalarycalculator.co.uk/salary.php"
            val response = Jsoup.connect(url)
                .timeout(30000)
                .method(Connection.Method.GET)
                .execute()

            val sessionCookies: Map<String, String> = response.cookies()
            val document = response.parse()
            val searchForm = document.getElementsByAttributeValue("action", "salary.php").first() as FormElement

            val searchInput = searchForm.getElementsByAttributeValue("name", "salary").first() as Element
            searchInput.text("$salary")

            val searchResponse = searchForm.submit()
                .timeout(30000)
                .header("Accept","text/html")
                .header("Content-Type","application/x-www-form-urlencoded")
                .cookies(sessionCookies)
                .method(Connection.Method.POST)
                .execute()
            val searchResults = searchResponse.parse()
            for (element in searchResults.select("table.results > tr")) {
                println("----------")
                println(element.text())
            }
            println("----------")
            salary += 10000
        } while (response.statusCode() == 200 && salary <= maxSalary)
    }


}
