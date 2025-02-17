(ns oauth.client-test
  (:require [oauth.client :as oc]
            [oauth.signature :as sig]
            :reload-all)
  (:use [clojure.pprint :only [pprint]]
        [clojure.test]))

(deftest ^{:doc "Test creation of authorization header for request_token access."}
         request-access-authorization-header
  (let [c (oc/make-consumer "GDdmIQH6jhtmLUypg82g"
                            "MCD8BKwGdgPHvAuvgvz4EQpqDAtx89grbuNMRd7Eh98"
                            "https://api.twitter.com/oauth/request_token"
                            "https://api.twitter.com/oauth/access_token"
                            "https://api.twitter.com/oauth/authorize"
                            :hmac-sha1)
        ;; Ensure that the params from Twitter example are used.
        unsigned-params (merge (sig/oauth-params c "QP70eNmVz8jvdPevU3oJD2AfF7R7odC2XJcn4XlZJqk" 1272323042)
                               {:oauth_callback         "http://localhost:3005/the_dance/process_callback?service_provider_id=11"
                                :oauth_consumer_key     "GDdmIQH6jhtmLUypg82g"
                                :oauth_signature_method "HMAC-SHA1"
                                :oauth_version          "1.0"})
        signature (sig/sign c (sig/base-string "POST"
                                               (:request-uri c)
                                               unsigned-params))
        params (assoc unsigned-params
                 :oauth_signature signature)]
    ;; Can't easily test this since params are in undefined order in header.
    (is (= (oc/authorization-header (sort params))
           "OAuth oauth_callback=\"http%3A%2F%2Flocalhost%3A3005%2Fthe_dance%2Fprocess_callback%3Fservice_provider_id%3D11\", oauth_consumer_key=\"GDdmIQH6jhtmLUypg82g\", oauth_nonce=\"QP70eNmVz8jvdPevU3oJD2AfF7R7odC2XJcn4XlZJqk\", oauth_signature=\"8wUi7m5HFQy76nowoCThusfgB%2BQ%3D\", oauth_signature_method=\"HMAC-SHA1\", oauth_timestamp=\"1272323042\", oauth_version=\"1.0\""))))

(deftest ^{:doc "Test creation of authorization header for access_token request"}
         access-token-authorization-header
  (let [c (oc/make-consumer "GDdmIQH6jhtmLUypg82g"
                            "MCD8BKwGdgPHvAuvgvz4EQpqDAtx89grbuNMRd7Eh98"
                            "https://api.twitter.com/oauth/request_token"
                            "https://api.twitter.com/oauth/access_token"
                            "https://api.twitter.com/oauth/authorize"
                            :hmac-sha1)
        ;; Ensure that the params from Twitter example are used.
        unsigned-params (merge (sig/oauth-params c "9zWH6qe0qG7Lc1telCn7FhUbLyVdjEaL3MO5uHxn8" 1272323047)
                               {:oauth_consumer_key     "GDdmIQH6jhtmLUypg82g"
                                :oauth_signature_method "HMAC-SHA1"
                                :oauth_token            "8ldIZyxQeVrFZXFOZH5tAwj6vzJYuLQpl0WUEYtWc"
                                :oauth_verifier         "pDNg57prOHapMbhv25RNf75lVRd6JDsni1AJJIDYoTY"
                                :oauth_version          "1.0"})
        signature (sig/sign c
                            (sig/base-string "POST"
                                             (:access-uri c)
                                             unsigned-params)
                            "x6qpRnlEmW9JbQn4PQVVeVG8ZLPEx6A0TOebgwcuA") ;; token secret
        params (assoc unsigned-params
                 :oauth_signature signature)]
    (is (= (oc/authorization-header (sort params))
           "OAuth oauth_consumer_key=\"GDdmIQH6jhtmLUypg82g\", oauth_nonce=\"9zWH6qe0qG7Lc1telCn7FhUbLyVdjEaL3MO5uHxn8\", oauth_signature=\"PUw%2FdHA4fnlJYM6RhXk5IU%2F0fCc%3D\", oauth_signature_method=\"HMAC-SHA1\", oauth_timestamp=\"1272323047\", oauth_token=\"8ldIZyxQeVrFZXFOZH5tAwj6vzJYuLQpl0WUEYtWc\", oauth_verifier=\"pDNg57prOHapMbhv25RNf75lVRd6JDsni1AJJIDYoTY\", oauth_version=\"1.0\""))))

(deftest ^{:doc "Test creation of approval URL"}
         user-approval-uri
  (let [c (oc/make-consumer "GDdmIQH6jhtmLUypg82g"
                            "MCD8BKwGdgPHvAuvgvz4EQpqDAtx89grbuNMRd7Eh98"
                            "https://api.twitter.com/oauth/request_token"
                            "https://api.twitter.com/oauth/access_token"
                            "https://api.twitter.com/oauth/authorize"
                            :hmac-sha1)
        t "nnch734d00sl2jdk"]
    ;; The approval URL should only use the :oauth_token in the User approval URI
    (is (= "https://api.twitter.com/oauth/authorize?oauth_token=nnch734d00sl2jdk"
           (oc/user-approval-uri c t)))
    (is (= "https://api.twitter.com/oauth/authorize?oauth_token=nnch734d00sl2jdk&extra=foo"
           (oc/user-approval-uri c t {:extra "foo"})))))

(deftest ^{:doc "Test creation of authorization header for refresh access_token request"}
         refresh-token-authorization-header
  (let [c (oc/make-consumer "GDdmIQH6jhtmLUypg82g"
                            "MCD8BKwGdgPHvAuvgvz4EQpqDAtx89grbuNMRd7Eh98"
                            "https://api.twitter.com/oauth/request_token"
                            "https://api.twitter.com/oauth/access_token"
                            "https://api.twitter.com/oauth/authorize"
                            :hmac-sha1)
        unsigned-params (merge (sig/oauth-params c "9zWH6qe0qG7Lc1telCn7FhUbLyVdjEaL3MO5uHxn8" 1272323047)
                               {:oauth_consumer_key     "GDdmIQH6jhtmLUypg82g"
                                :oauth_nonce            "9zWH6qe0qG7Lc1telCn7FhUbLyVdjEaL3MO5uHxn8"
                                :oauth_signature_method "HMAC-SHA1"
                                :oauth_token            "8ldIZyxQeVrFZXFOZH5tAwj6vzJYuLQpl0WUEYtWc"
                                :oauth_timestamp        "1272323047"
                                :oauth_version          "1.0"}
                               {:oauth_session_handle "5a10ddsqoqo2rfi"})
        signature (sig/sign c (sig/base-string "POST"
                                               (:request-uri c)
                                               unsigned-params))
        params (assoc unsigned-params
                 :oauth_signature signature)]
    (is (= (oc/authorization-header (sort params))
           "OAuth oauth_consumer_key=\"GDdmIQH6jhtmLUypg82g\", oauth_nonce=\"9zWH6qe0qG7Lc1telCn7FhUbLyVdjEaL3MO5uHxn8\", oauth_session_handle=\"5a10ddsqoqo2rfi\", oauth_signature=\"f15S84zVZ96f9PwAJrBHq28KIF4%3D\", oauth_signature_method=\"HMAC-SHA1\", oauth_timestamp=\"1272323047\", oauth_token=\"8ldIZyxQeVrFZXFOZH5tAwj6vzJYuLQpl0WUEYtWc\", oauth_version=\"1.0\""))))

(deftest override-host-test
  (testing "override host"
    (is (= (oc/override-host "https://magento2.mgt/index.php/oauth/token/request" "1234-180-123-89-218.eu.ngrok.io")
           "https://1234-180-123-89-218.eu.ngrok.io/index.php/oauth/token/request"))))

(deftest override-uri-test
  (testing "override uri"
    (let [consumer (oc/make-consumer "GDdmIQH6jhtmLUypg82g"
                                     "MCD8BKwGdgPHvAuvgvz4EQpqDAtx89grbuNMRd7Eh98"
                                     "https://api.twitter.com/oauth/request_token"
                                     "https://api.twitter.com/oauth/access_token"
                                     "https://api.twitter.com/oauth/authorize"
                                     :hmac-sha1)]
      (testing "no host override"
        (is (= (oc/override-uri consumer :request-uri)
               "https://api.twitter.com/oauth/request_token")))

      (testing "request-uri with override"
        (is (= (oc/override-uri (assoc consumer :override-host "1234-180-123-89-218.eu.ngrok.io") :request-uri)
               "https://1234-180-123-89-218.eu.ngrok.io/oauth/request_token")))

      (testing "access-uri with override"
        (is (= (oc/override-uri (assoc consumer :override-host "1234-180-123-89-218.eu.ngrok.io") :access-uri)
               "https://1234-180-123-89-218.eu.ngrok.io/oauth/access_token")))

      (testing "authorize with override"
        (is (= (oc/override-uri (assoc consumer :override-host "1234-180-123-89-218.eu.ngrok.io") :authorize-uri)
               "https://1234-180-123-89-218.eu.ngrok.io/oauth/authorize")))

      (testing "nil URI with override"
        (is (nil? (oc/override-uri (-> consumer
                                    (assoc :override-host "1234-180-123-89-218.eu.ngrok.io"
                                           :authorize-uri nil)) :authorize-uri)))))))
