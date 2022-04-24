from webbrowser import Chrome
from pathos.multiprocessing import ProcessingPool as newPool

from selenium import webdriver
from webdriver_manager.chrome import ChromeDriverManager
from selenium.webdriver.common.desired_capabilities import DesiredCapabilities

import time
from bs4 import BeautifulSoup

global G_HEADLESS
G_HEADLESS = "0"

def multi_parser(site):
    chromeOptions = webdriver.ChromeOptions()
    caps = DesiredCapabilities().CHROME 
    caps["pageLoadStrategy"] = "none"
    chromeOptions.add_argument('headless') # headless 모드 설정
    chromeOptions.add_argument("window-size=1920x1080") # 화면크기(전체화면)
    chromeOptions.add_argument("disable-gpu") 
    chromeOptions.add_argument("disable-infobars")
    chromeOptions.add_argument("--disable-extensions")
    prefs = {'profile.default_content_setting_values': {'cookies' : 2, 'images': 2, 'plugins' : 2, 'popups': 2, 'geolocation': 2, 'notifications' : 2, 'auto_select_certificate': 2, 'fullscreen' : 2, 'mouselock' : 2, 'mixed_script': 2, 'media_stream' : 2, 'media_stream_mic' : 2, 'media_stream_camera': 2, 'protocol_handlers' : 2, 'ppapi_broker' : 2, 'automatic_downloads': 2, 'midi_sysex' : 2, 'push_messaging' : 2, 'ssl_cert_decisions': 2, 'metro_switch_to_desktop' : 2, 'protected_media_identifier': 2, 'app_banner': 2, 'site_engagement' : 2, 'durable_storage' : 2}}   
    chromeOptions.add_experimental_option('prefs', prefs)
    driver = webdriver.Chrome(executable_path='chromedriver', chrome_options=chromeOptions)
    driver.implicitly_wait(3)
    return parse(driver, site)


def parse(driver, site):
    driver.get(site)
    
    try:
        pic = driver.find_element_by_class_name('_img')
        pic = pic.get_attribute('src')
        pic=pic.replace('type=f180_180&','type=w750&')
        return
    except:
        # while(driver.page_source[38] != "네"):
        for i in range(10000000000):
            driver.get(site)
            time.sleep(0.1)
            if driver.page_source[38] == "네":
                break
    pic = driver.find_element_by_class_name('_img')
    pic = pic.get_attribute('src')
    pic=pic.replace('type=f180_180&','type=w750&')

    
if __name__ == '__main__': # ， ，pathos
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
    # https://m.store.naver.com/places/detail?id=36271040
        link=link.replace('store','place')
        link=link.replace('places','restaurant')
        link=link[:37]+link[link.find('=')+1:]+'/photo?filterType=음식'
        
        list.append(link)
    pool = newPool(process=4)
    pool.map(multi_parser, list)
    pool.close()
    pool.join()
    driver.close()
    print("\n Multiprocessing time :", time.time() - start)
