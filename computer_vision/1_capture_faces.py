"""
Step 1: Capture face images from your laptop camera for a given person.

Usage:
    python 1_capture_faces.py
    python 1_capture_faces.py --name "Edwin" --samples 60

Press 'q' to quit early. Images are saved to dataset/<face_id>_<name>/
"""

import argparse
import os

import cv2

BASE_DIR = os.path.dirname(os.path.abspath(__file__))
DATASET_DIR = os.path.join(BASE_DIR, "dataset")
LABELS_FILE = os.path.join(BASE_DIR, "labels.txt")


def get_or_create_face_id(name: str) -> int:
    """Look up an existing numeric id for `name`, or assign the next free one."""
    labels = {}
    if os.path.exists(LABELS_FILE):
        with open(LABELS_FILE, "r") as f:
            for line in f:
                line = line.strip()
                if not line:
                    continue
                face_id, label = line.split(",", 1)
                labels[label] = int(face_id)

    if name in labels:
        return labels[name]

    next_id = max(labels.values(), default=0) + 1
    labels[name] = next_id

    with open(LABELS_FILE, "w") as f:
        for label, face_id in labels.items():
            f.write(f"{face_id},{label}\n")

    return next_id


def capture(name: str, num_samples: int, camera_index: int) -> None:
    face_id = get_or_create_face_id(name)
    person_dir = os.path.join(DATASET_DIR, f"{face_id}_{name}")
    os.makedirs(person_dir, exist_ok=True)

    detector = cv2.CascadeClassifier(
        cv2.data.haarcascades + "haarcascade_frontalface_default.xml"
    )

    cam = cv2.VideoCapture(camera_index, cv2.CAP_AVFOUNDATION)
    if not cam.isOpened():
        raise RuntimeError(
            f"Could not open camera index {camera_index}. "
            "Try a different --camera value (0, 1, ...)."
        )

    # macOS's AVFoundation backend returns black frames for the first
    # moment while the sensor warms up; discard a few before starting.
    for _ in range(30):
        cam.read()

    print(f"[INFO] Capturing images for '{name}' (id={face_id}).")
    print("[INFO] Look at the camera. Press 'q' to stop early.")

    count = 0
    while count < num_samples:
        ok, frame = cam.read()
        if not ok:
            print("[WARN] Failed to read frame from camera.")
            break

        gray = cv2.cvtColor(frame, cv2.COLOR_BGR2GRAY)
        faces = detector.detectMultiScale(gray, scaleFactor=1.2, minNeighbors=5)

        for (x, y, w, h) in faces:
            count += 1
            face_img = gray[y : y + h, x : x + w]
            out_path = os.path.join(person_dir, f"{count}.jpg")
            cv2.imwrite(out_path, face_img)

            cv2.rectangle(frame, (x, y), (x + w, y + h), (0, 255, 0), 2)
            cv2.putText(
                frame,
                f"Samples: {count}/{num_samples}",
                (10, 30),
                cv2.FONT_HERSHEY_SIMPLEX,
                0.8,
                (0, 255, 0),
                2,
            )
            break  # only take one face per frame

        cv2.imshow("Capturing faces - press q to quit", frame)
        if cv2.waitKey(1) & 0xFF == ord("q"):
            break

    cam.release()
    cv2.destroyAllWindows()
    print(f"[INFO] Done. Saved {count} images to {person_dir}")


if __name__ == "__main__":
    parser = argparse.ArgumentParser(description="Capture face samples from webcam.")
    parser.add_argument("--name", help="Name of the person being captured.")
    parser.add_argument(
        "--samples", type=int, default=50, help="Number of face samples to capture."
    )
    parser.add_argument(
        "--camera", type=int, default=0, help="Camera index (default 0)."
    )
    args = parser.parse_args()

    name = args.name or input("Enter the person's name: ").strip()
    if not name:
        raise SystemExit("Name cannot be empty.")

    capture(name, args.samples, args.camera)
