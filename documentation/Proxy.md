### Proxy Setup

## Port-forwarding

In the case that the mobile phones cannot directly access the proxy, we have to configure port forwarding from the crawler (where the phones are connected).

Use the following script:
```
for i in {10000..10009} {38080..38088} {39000..39001}; do
  iptables -t nat -A PREROUTING -p tcp -i enx3c18a0432c49 -s CRAWLER_IP/24 --dport "$i" -j DNAT --to-destination "PROXY_IP:$i"
done
```

## Browser Port Assignments

| Browser Type | Name                           | Port  |
| ------------ | ------------------------------ | ----- |
| Remote       | Chrome 72                      | 10000 |
| Remote       | Chrome 72 b                    | 10001 |
| Remote       | Chrome 72 scroll               | 10002 |
| Remote       | Firefox 65                     | 10003 |
| Remote       | Firefox 65 ghostery            | 10004 |
| Remote       | Firefox 45                     | 10005 |
| Remote       | Brave                          | 10006 |
| Remote       | Tor                            | 10007 |
| Remote       | Chrome 72 Selenium             | 10008 |
| Remote       | Firefox 65 Selenium            | 10009 |
| Local        | OpenWPM-mobile Firefox 45      | 39000 |
| Local        | OpenWPM-mobile Firefox 65      | 39001 |
| Mobile       | Tor                            | 38080 |
| Mobile       | Chrome with desktop user agent | 38081 |
| Mobile       | Chrome                         | 38082 |
| Mobile       | Firefox                        | 38083 |
| Mobile       | Ghostery                       | 38084 |
| Mobile       | Brave                          | 38085 |
| Mobile       | Firefox Focus                  | 38086 |
| Mobile       | Chrome scroll                  | 38087 |
| Mobile       | DuckDuckGo                     | 38088 |
