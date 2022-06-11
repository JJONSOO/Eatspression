from deepface import DeepFace

from model import *
from emotion import *

model, dense = loadModel()

def analyze(img_list):
    emo_list, vec_list = get_emo_vec(img_list, model, dense)
    
    init_emo = calc_emoSim(emo_list[0][0][0])
    init_vec = vec_list[0][0]
    
    del emo_list[0]
    del vec_list[0]
    
    cos_idx = -1
    cos_score = 0
    
    for idx, l in enumerate(zip(emo_list, vec_list)):
        for emo, vec in zip(l[0], l[1]):
            emo_sim = calc_emoSim(emo[0])
            cos_sim = calc_cosSim(init_vec, vec)
            if abs(abs(init_emo) - abs(emo_sim)) > 40:
                continue
            if cos_idx == -1 or cos_score < cos_sim:
                cos_idx= idx
                cos_score = cos_sim
    
    return idx

# # all average
# def analyze(img_list):
#     emo_list, vec_list = get_emo_vec(img_list)
    
#     init_emo = calc_emoSim(emo_list[0][0][0])
#     init_vec = vec_list[0][0]
    
#     del emo_list[0]
#     del vec_list[0]
    
#     cos_idx = -1
#     cos_score = 0
    
#     for idx, l in enumerate(zip(emo_list, vec_list)):
#         emo_sim, cos_sim = 0, 0
#         cnt = 0
#         for emo, vec in zip(l[0], l[1]):
#             emo = calc_emoSim(emo[0])
#             if abs(abs(init_emo) - abs(emo)) < 40:
#                 cnt += 1
#                 emo_sim += calc_emoSim(emo)
#                 cos_sim += calc_cosSim(init_vec, vec)
#         cos_sim /= cnt
        # if cos_idx == -1 or cos_score < cos_sim:
        #     cos_idx = idx
        #     cos_score = cos_sim