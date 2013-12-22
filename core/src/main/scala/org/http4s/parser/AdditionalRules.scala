package org.http4s
package parser

import org.parboiled2._
import scala.util.Try
import org.joda.time.{DateTimeZone, DateTime}
import shapeless.{HNil, ::}

/**
 * @author Bryce Anderson
 *         Created on 12/22/13
 */
private[parser] trait AdditionalRules extends Rfc2616BasicRules { this: Parser =>

  def Digits: Rule1[String] = rule { capture(zeroOrMore( Digit )) }

  def Value = rule { Token | QuotedString }

  def Parameter: Rule1[(String,String)] = rule { Token ~ "=" ~ Value ~> ((_: String, _: String)) }

  def HttpDate: Rule1[DateTime] = rule { (RFC1123Date | RFC850Date | ASCTimeDate) ~ OptWS }

  def RFC1123Date: Rule1[DateTime] = rule {
  Wkday ~ str(", ") ~ Date1 ~ ch(' ') ~ Time ~ ch(' ') ~ (str("GMT") | str("UTC")) ~> {
    (year: Int, hour: Int, min: Int, sec: Int) =>
            createDateTime(year, _:Int, _:Int, hour, min, sec, _:Int)
        } ~> {
          (wkday: Int, day: Int, month: Int, f: Function3[Int, Int, Int, DateTime]) =>
            f(month, day, wkday)
        }
  }

  def RFC850Date: Rule1[DateTime] = rule {
    Weekday ~ str(", ") ~ Date2 ~ ch(' ') ~ Time ~ ch(' ') ~ (str("GMT") | str("UTC")) ~> {
      (year: Int, hour: Int, min: Int, sec: Int) =>
        createDateTime(year, _:Int, _:Int, hour, min, sec, _:Int)
    } ~> {
      (wkday: Int, day: Int, month: Int, f: Function3[Int, Int, Int, DateTime]) =>
        f(month, day, wkday)
    }
  }

  def ASCTimeDate: Rule1[DateTime] = rule {
    Wkday ~ ch(' ') ~ Date3 ~ ch(' ') ~ Time ~ ch(' ') ~ Digit4 ~> {
      (hour:Int, min:Int, sec:Int, year:Int) =>
        createDateTime(year, _:Int, _:Int, hour, min, sec, _:Int)
      } ~> { (wkday:Int, month:Int, day:Int, f: (Int, Int, Int) => DateTime) =>
        f(month, day, wkday)
      }
  }

  def Date1: RuleN[Int::Int::Int::HNil] = rule { Digit2 ~ ch(' ') ~ Month ~ ch(' ') ~ Digit4 }

  def Date2: RuleN[Int::Int::Int::HNil] = rule { Digit2 ~ ch('-') ~ Month ~ ch('-') ~ Digit4 }

  def Date3: Rule2[Int, Int] = rule { Month ~ ch(' ') ~ (Digit2 | ch(' ') ~ Digit1) }

  def Time: RuleN[Int::Int::Int::HNil] = rule { Digit2 ~ ch(':') ~ Digit2 ~ ch(':') ~ Digit2 }

  def Wkday: Rule1[Int] = rule { ("Sun" ~ push(0)) |
                                 ("Mon" ~ push(1)) |
                                 ("Tue" ~ push(2)) |
                                 ("Wed" ~ push(3)) |
                                 ("Thu" ~ push(4)) |
                                 ("Fri" ~ push(5)) |
                                 ("Sat" ~ push(6)) }

  def Weekday: Rule1[Int] = rule { ("Sunday"   ~ push(0)) |
                                   ("Monday"   ~ push(1)) |
                                   ("Tuesday"  ~ push(2)) |
                                   ("Wedsday"  ~ push(3)) |
                                   ("Thursday" ~ push(4)) |
                                   ("Friday"   ~ push(5)) |
                                   ("Saturday" ~ push(6)) }

  def Month: Rule1[Int] = rule {  ("Jan" ~ push(1))  |
                                  ("Feb" ~ push(2))  |
                                  ("Mar" ~ push(3))  |
                                  ("Apr" ~ push(4))  |
                                  ("May" ~ push(5))  |
                                  ("Jun" ~ push(6))  |
                                  ("Jul" ~ push(7))  |
                                  ("Aug" ~ push(8))  |
                                  ("Sep" ~ push(9))  |
                                  ("Oct" ~ push(10)) |
                                  ("Nov" ~ push(11)) |
                                  ("Dec" ~ push(12)) }

  def Digit1: Rule1[Int] = rule { capture(Digit) ~> {s: String => s.toInt} }

  def Digit2: Rule1[Int] = rule { capture(Digit ~ Digit) ~> {s: String => s.toInt} }

  def Digit4: Rule1[Int] = rule { capture(Digit ~ Digit ~ Digit ~ Digit) ~> {s: String => s.toInt} }

  private def createDateTime(year: Int, month: Int, day: Int, hour: Int, min: Int, sec: Int, wkday: Int) = {
    Try(new DateTime(year, month, day, hour, min, sec, DateTimeZone.UTC)).getOrElse {
      // TODO Would be better if this message had the real input.
      throw new Exception("Invalid date: "+year+"-"+month+"-"+day+" "+hour+":"+min+":"+sec )
    }
  }

//  def Ip: Rule1[InetAddress] = rule (
//    group(IpNumber ~ ch('.') ~ IpNumber ~ ch('.') ~ IpNumber ~ ch('.') ~ IpNumber) ~> (InetAddress.getByName(_)) ~ OptWS
//  )
//
//  def IpNumber = rule {
//    Digit ~ optional(Digit ~ optional(Digit))
//  }
//
//  def AuthScheme = rule {
//    Token ~ OptWS
//  }
//
//  def AuthParam = rule {
//    Token ~ "=" ~ (Token | QuotedString) ~~> ((_, _))
//  }
}