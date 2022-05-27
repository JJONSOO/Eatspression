from flask import Blueprint
from flask import request
from flask_restx import Namespace,Resource
import sys
import os
sys.path.append(os.path.dirname(os.path.abspath(os.path.dirname(__file__))))
from model import Restaurant
import json
import requests
import base64
import io
from PIL import ImageFile
from PIL import Image
import matplotlib.pyplot as plt
import threading

import time


lock = threading.Lock()
User_Info={}
User_ID=0


user_api = Namespace('user_api')
@user_api.route("/")
class User_location(Resource):
    def get(self):
        return "get"
    def post(self):
        lock.acquire()
        global User_ID
        User_ID+=1
    

        #request 정보 받아오기
        point = request.json
        x=point['x']
        y=point['y']
        distance=point['dist']
        num=point['recommendNum']

        #Restaurant_Info 받아오기
        Restaurant_Info=Restaurant.get_food_list(x,y,distance,num)
        Restaurant_Info=dict(zip(range(1, len(Restaurant_Info) + 1), Restaurant_Info))
        User_Info[str(User_ID)]=[]
        User_Info[str(User_ID)].append(Restaurant_Info)
        for i in range(len(Restaurant_Info)):
            list=[]
            User_Info[str(User_ID)].append(list)
        Restaurant_Info['user_id']=User_ID
        lock.release()
        return Restaurant_Info


@user_api.route("/image")
class User_Face(Resource):
    def get(self):
        return "get"
    def post(self):
        lock.acquire()

        img_data=request.json
        image=img_data['image']
        img_num=int(img_data['img_num'])
        cur_user_id=img_data['user_id']
        # print('user_id: ',cur_user_id,'img_num: ',img_num+1)
        User_Info[str(cur_user_id)][(img_num)+1].append(image)
        lock.release()

        return "김유민 바보"

@user_api.route("/finish")
class Finish(Resource):
    def get(self):
        return "get"
    def post(self):
        lock.acquire()


        user_id=request.json['user_id']
        URL='http://b9be-34-66-47-127.ngrok.io/receive'
        

        json_data={'face_info':User_Info[str(user_id)][1:]}
        idx=requests.post(URL,json=json_data)

        idx=idx.json()['idx']
        idx=int(idx)+1
        address=Restaurant.get_food_link(User_Info[str(user_id)][0][idx])

        #음식 사진 , 이름 , 주소
        json={'img':User_Info[str(user_id)][0][idx] , 'address':address}
        lock.release()

        return json
@user_api.route("/custom")
class Custom(Resource):
    def get(self):
        return "get"
    def post(self):
        lock.acquire()
        global User_ID
        User_ID+=1
        img_num=request.json['num']
        User_Info[str(User_ID)]=[]
        for i in range(img_num):
            list=[]
            User_Info[str(User_ID)].append(list)
        json = {'user_id':User_ID}
        lock.release()
        return json

@user_api.route("/custom/image")
class Custom_image(Resource):
    def get(self):
        return "get"
    def post(self):
        lock.acquire()
        img_data=request.json
        image=img_data['image']
        img_num=int(img_data['img_num'])
        cur_user_id=img_data['user_id']
        # print('user_id: ',cur_user_id,'img_num: ',img_num+1)
        User_Info[str(cur_user_id)][(img_num)].append(image)
        lock.release()
        return "김유민 바보"

@user_api.route("/custom/finish")
class Finish(Resource):
    def get(self):
        return "get"
    def post(self):
        lock.acquire()

        user_id=request.json['user_id']
        URL='http://b9be-34-66-47-127.ngrok.io/receive'
        

        json_data={'face_info':User_Info[str(user_id)]}
        idx=requests.post(URL,json=json_data)

        idx=idx.json()['idx']
        json={'idx':idx}
        lock.release()

        return json