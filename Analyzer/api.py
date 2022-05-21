import tensorflow as tf
import keras
from keras.models import Model, Sequential
from keras.layers import Conv2D, MaxPooling2D, AveragePooling2D, Flatten, Dense, Dropout
import numpy as np
from deepface import DeepFace
from deepface.commons import functions, realtime, distance as dst

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
    model.pop()
    return model

model = loadModel()

def preprocess(path):
    img, _ = functions.preprocess_face(img = path, target_size = (48, 48), grayscale = True, enforce_detection = True, detector_backend = 'opencv', return_region = True)
    return img

def to_emo(img):
    return DeepFace.analyze(img_path = img, actions = ['emotion'])['emotion']

def to_vec(img):
    model = loadModel()
    return tf.squeeze(model(preprocess(img)))

def get_emo_vec(img_list):
    emo_list = []
    vec_list = []
    
    init_img = img_list[0][0]
    
    emo_list.append(to_emo(init_img))
    vec_list.append(to_vec(init_img))
    
    
    for imgs in img_list:
        emo_tmp = []
        vec_tmp = []
        for img in imgs:
            emo_tmp.append(to_emo(img))
            vec_tmp.append(to_vec(img))
        emo_list.append(emo_tmp)
        vec_list.append(vec_tmp)
        
    return emo_list, vec_list

def calc_cosSim(vec1, vec2):
    return cosine_loss(vec1, vec2).numpy()

def calc_emoSim(emo):
    pos = 0
    neg = 0
    for k, v in emo.items():
        if k in POSITIVE:
            pos += v
        elif k in NEGATIVE:
            neg += v
    return pos - neg

def analyze(img_list):
    emo_list, vec_list = get_emo_vec(img_list)
    
    init_emo = emo_list[0][0]
    init_vec = vec_list[0][0]
    
    emo_idx, cos_idx = -1, -1
    emo_score, cos_score = init_emo, 0
    
    for idx, l in enumerate(zip(emo_list, vec_list)):
        for emo, vec in l:
            emo_sim = calc_emoSim(emo)
            cos_sim = calc_cosSim(init_vec, vec)
            if emo_idx == -1 or emo_score <= emo_sim:
                if cos_idx == -1 or cos_score < cos_sim:
                    cos_idx, emo_idx = idx, idx
                    emo_score, cos_score = emo_sim, cos_sim
    
    return idx