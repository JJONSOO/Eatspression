import tensorflow as tf
import keras
from keras.models import Model, Sequential
from keras.layers import Conv2D, MaxPooling2D, AveragePooling2D, Flatten, Dense, Dropout
import numpy as np
from deepface import DeepFace
from deepface.commons import functions, realtime, distance as dst
import os
import gdown

cosine_loss = tf.keras.losses.CosineSimilarity()

POSITIVE = ['happy', 'neutral', 'surprise']
NEGATIVE = ['angry', 'disgust', 'fear', 'sad']


def loadModel(url = 'https://github.com/serengil/deepface_models/releases/download/v1.0/facial_expression_model_weights.h5'):

    num_classes = 7

    model = Sequential()

    #1st convolution layer
    model.add(Conv2D(64, (5, 5), activation='relu', input_shape=(48,48,1)))
    model.add(MaxPooling2D(pool_size=(5,5), strides=(2, 2)))

    #2nd convolution layer
    model.add(Conv2D(64, (3, 3), activation='relu'))
    model.add(Conv2D(64, (3, 3), activation='relu'))
    model.add(AveragePooling2D(pool_size=(3,3), strides=(2, 2)))

    #3rd convolution layer
    model.add(Conv2D(128, (3, 3), activation='relu'))
    model.add(Conv2D(128, (3, 3), activation='relu'))
    model.add(AveragePooling2D(pool_size=(3,3), strides=(2, 2)))

    model.add(Flatten())

    #fully connected neural networks
    model.add(Dense(1024, activation='relu'))
    model.add(Dropout(0.2))
    model.add(Dense(1024, activation='relu'))
    model.add(Dropout(0.2))

    model.add(Dense(num_classes, activation='softmax'))

    #----------------------------

    home = functions.get_deepface_home()

    if os.path.isfile(home+'/.deepface/weights/facial_expression_model_weights.h5') != True:
        print("facial_expression_model_weights.h5 will be downloaded...")

        output = home+'/.deepface/weights/facial_expression_model_weights.h5'
        gdown.download(url, output, quiet=False)

        """
        #google drive source downloads zip
        output = home+'/.deepface/weights/facial_expression_model_weights.zip'
        gdown.download(url, output, quiet=False)
        #unzip facial_expression_model_weights.zip
        with zipfile.ZipFile(output, 'r') as zip_ref:
            zip_ref.extractall(home+'/.deepface/weights/')
        """

    model.load_weights(home+'/.deepface/weights/facial_expression_model_weights.h5')
    dense = Sequential()
    dense_layer = model.get_layer(index=-1)
    dense.add(dense_layer)
    model.pop()
    model.pop()
    return model, dense

model, dense = loadModel()

def preprocess(path):
    img, _ = functions.preprocess_face(img = path, target_size = (48, 48), grayscale = True, enforce_detection = True, detector_backend = 'opencv', return_region = True)
    return img

def to_emo(vec):
    # angry disgust fear happy sad surprise neutral
    return dense(vec)

def to_vec(img):
    return model(preprocess(img))

def get_emo_vec(img_list):
    emo_list = []
    vec_list = []
    
    init_img = img_list[0][0]
    
    init_emo = []
    init_vec = []
    vec = to_vec(init_img)
    init_emo.append(to_emo(vec))
    init_vec.append(tf.squeeze(vec))
    emo_list.append(init_emo)
    vec_list.append(init_vec)
    
    
    for imgs in img_list:
        emo_tmp = []
        vec_tmp = []
        for img in imgs:
            vec = to_vec(img)
            emo_tmp.append(to_emo(vec))
            vec_tmp.append(tf.squeeze(vec))
        emo_list.append(emo_tmp)
        vec_list.append(vec_tmp)
        
    return emo_list, vec_list

def calc_cosSim(vec1, vec2):
    return cosine_loss(vec1, vec2).numpy()

def calc_emoSim(emo):
    pos = 0
    neg = 0
    pos += emo[3]
    pos += emo[-1]
    neg += emo[4]
    return pos - neg

def analyze(img_list):
    emo_list, vec_list = get_emo_vec(img_list)
    
    init_emo = calc_emoSim(emo_list[0][0])
    init_vec = vec_list[0][0]
    
    del emo_list[0]
    del vec_list[0]
    
    emo_idx, cos_idx = -1, -1
    emo_score, cos_score = init_emo, 0
    
    for idx, l in enumerate(zip(emo_list, vec_list)):
        for emo, vec in zip(l[0], l[1]):
            emo_sim = calc_emoSim(emo)
            cos_sim = calc_cosSim(init_vec, vec)
            if abs(abs(init_emo) - abs(emo_sim)) > 40:
                continue
            if emo_idx == -1 or emo_score <= emo_sim:
                if cos_idx == -1 or cos_score < cos_sim:
                    cos_idx, emo_idx = idx, idx
                    emo_score, cos_score = emo_sim, cos_sim
    
    return idx

# # all average
# def analyze(img_list):
#     emo_list, vec_list = get_emo_vec(img_list)
    
#     init_emo = calc_emoSim(emo_list[0][0])
#     init_vec = vec_list[0][0]
    
#     del emo_list[0]
#     del vec_list[0]
    
#     emo_idx, cos_idx = -1, -1
#     emo_score, cos_score = init_emo, 0
    
#     for idx, l in enumerate(zip(emo_list, vec_list)):
#         emo_sim, cos_sim = 0, 0
#         cnt = 0
#         for emo, vec in zip(l[0], l[1]):
#             emo = calc_emoSim(emo)
#             if abs(abs(init_emo) - abs(emo)) < 40:
#                 cnt += 1
#                 emo_sim += calc_emoSim(emo)
#                 cos_sim += calc_cosSim(init_vec, vec)
#         emo_sim /= cnt
#         cos_sim /= cnt
#         if emo_idx == -1 or emo_score <= emo_sim:
#             if cos_idx == -1 or cos_score < cos_sim:
#                 cos_idx, emo_idx = idx, idx
#                 emo_score, cos_score = emo_sim, cos_sim
            