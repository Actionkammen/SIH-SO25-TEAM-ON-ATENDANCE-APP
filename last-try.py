from deepface import DeepFace
import cv2
import os
import time

# Folder containing known faces
dataset_path = "C:/Users/Vaish/Desktop/face"

# Load all known faces into dictionary
known_faces = {}
for file in os.listdir(dataset_path):
    if file.lower().endswith((".jpg", ".png", ".jpeg")):
        name = os.path.splitext(file)[0]  # use filename as person name
        known_faces[name] = os.path.join(dataset_path, file)

# Start webcam
video_cap = cv2.VideoCapture(0)
frame_width = int(video_cap.get(3))
frame_height = int(video_cap.get(4))

out = cv2.VideoWriter("output.avi",
                      cv2.VideoWriter_fourcc(*'XVID'),
                      20, (frame_width, frame_height))

start_time = time.time()
frame_count = 0
result_text = "Scanning..."

while True:
    ret, frame = video_cap.read()
    if not ret:
        break

    frame_count += 1
    small_frame = cv2.resize(frame, (480, 360))

    # Run recognition every 10th frame
    if frame_count % 10 == 0:
        result_text = "Unknown"
        try:
            for name, path in known_faces.items():
                result = DeepFace.verify(
                    img1_path=path,
                    img2_path=small_frame,
                    detector_backend="opencv",
                    enforce_detection=False
                )
                if result["verified"]:
                    result_text = "Matched"
                    break
        except Exception:
            result_text = "No face detected"

    # Show result on video
    cv2.putText(frame, result_text, (50, 50),
                cv2.FONT_HERSHEY_SIMPLEX, 1, (0, 255, 0), 2)

    out.write(frame)
    cv2.imshow("Video Live", frame)

    if time.time() - start_time > 30:
        print("Recording finished: 30 seconds")
        break

    if cv2.waitKey(10) & 0xFF == ord("a"):
        break

video_cap.release()
out.release()
cv2.destroyAllWindows()
