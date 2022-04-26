import re
import requests 
from webbrowser import Chrome
from pathos.multiprocessing import ProcessingPool as newPool

from selenium import webdriver
from webdriver_manager.chrome import ChromeDriverManager
from selenium.webdriver.common.desired_capabilities import DesiredCapabilities

import time
from bs4 import BeautifulSoup

global G_HEADLESS
G_HEADLESS = "0"
    
start=time.time()
URL="https://s.search.naver.com/p/around/search.naver?tab=restaurant&lat=37.451891&lng=126.6543237&rid=11177540"
# URL="https://m.place.naver.com/restaurant/36271040/review/visitor"
chromeOptions = webdriver.ChromeOptions()
chromeOptions.add_argument("user-agent=Mozilla/5.0 (Macintosh; Intel Mac OS X 10_12_6) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/61.0.3163.100 Safari/537.36")
chromeOptions.add_argument("headless")
chromeOptions.add_argument("window-size=1920x1080")
chromeOptions.add_argument("disable-gpu")
chromeOptions.add_argument("lang=ko_KR") # 한국어!

driver = webdriver.Chrome(executable_path='chromedriver', chrome_options=chromeOptions)

driver.get(url=URL)
links = driver.find_elements_by_class_name('bx_area')
links=links[:15]
list=[]
for link in links:
    link=link.get_attribute('href')
    link=link.replace('store','place')
    link=link.replace('places','restaurant')
    link=link[:37]+link[link.find('=')+1:]+'/review/visitor'
    
    list.append(link)
for idx in range(len(list)):
    print(list[idx])

    URL = list[idx]
    response = requests.get(URL) 
    response.status_code
    a=response.text.strip("\u002F")


    mask = re.compile('"imgUrl":[\S]+",')
    rmmask = re.compile('("imgUrl":")|(")|,')
    result = []
    for res in mask.findall(a):
        result.append(rmmask.sub('', res).replace('\\u002F', '/'))
    print(result[0])
    print('idx: ',idx)
print("\ntime :", time.time() - start)
