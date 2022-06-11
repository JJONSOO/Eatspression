from deepface.commons import functions
import tensorflow as tf

def preprocess(path):
    img, _ = functions.preprocess_face(img = path, target_size = (48, 48), grayscale = True, enforce_detection = True, detector_backend = 'opencv', return_region = True)
    return img

def to_emo(vec, dense):
    # angry disgust fear happy sad surprise neutral
    return dense(vec)

def to_vec(img, model):
    return model(preprocess(img))

def get_emo_vec(img_list, model, dense):
    emo_list = []
    vec_list = []
    
    init_img = img_list[0][0]
    
    init_emo = []
    init_vec = []
    vec = to_vec(init_img, model)
    init_emo.append(to_emo(vec, dense))
    init_vec.append(tf.squeeze(vec))
    emo_list.append(init_emo)
    vec_list.append(init_vec)
    
    
    for imgs in img_list:
        emo_tmp = []
        vec_tmp = []
        for img in imgs:
            vec = to_vec(img, model)
            emo_tmp.append(to_emo(vec, dense))
            vec_tmp.append(tf.squeeze(vec))
        emo_list.append(emo_tmp)
        vec_list.append(vec_tmp)
        
    return emo_list, vec_list

def calc_cosSim(vec1, vec2, cosine_loss):
    return cosine_loss(vec1, vec2).numpy()

def calc_emoSim(emo):
    pos = 0
    neg = 0
    pos += emo[3]
    pos += emo[-1]
    neg += emo[4]
    return pos - neg