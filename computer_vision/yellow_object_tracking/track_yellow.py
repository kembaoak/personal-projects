"""
Real-time yellow object tracking using your laptop camera.

Usage:
    python track_yellow.py
    python track_yellow.py --camera 1 --min-area 800

Press 'q' to quit.
"""

import argparse

import cv2
import numpy as np

# HSV range for yellow. Hue ~30 is yellow in OpenCV's 0-179 hue scale.
LOWER_YELLOW = np.array([20, 100, 100])
UPPER_YELLOW = np.array([35, 255, 255])


def main():
    parser = argparse.ArgumentParser(description="Real-time yellow object tracking.")
    parser.add_argument("--camera", type=int, default=0, help="Camera index.")
    parser.add_argument(
        "--min-area",
        type=int,
        default=500,
        help="Minimum contour area (pixels) to count as a tracked object.",
    )
    args = parser.parse_args()

    cam = cv2.VideoCapture(args.camera, cv2.CAP_AVFOUNDATION)
    if not cam.isOpened():
        raise RuntimeError(
            f"Could not open camera index {args.camera}. "
            "Try a different --camera value (0, 1, ...)."
        )

    # macOS's AVFoundation backend returns black frames for the first
    # moment while the sensor warms up; discard a few before starting.
    for _ in range(30):
        cam.read()

    print("[INFO] Tracking yellow objects. Press 'q' to quit.")

    while True:
        ok, frame = cam.read()
        if not ok:
            print("[WARN] Failed to read frame from camera.")
            break

        blurred = cv2.GaussianBlur(frame, (11, 11), 0)
        hsv = cv2.cvtColor(blurred, cv2.COLOR_BGR2HSV)
        mask = cv2.inRange(hsv, LOWER_YELLOW, UPPER_YELLOW)
        mask = cv2.erode(mask, None, iterations=2)
        mask = cv2.dilate(mask, None, iterations=2)

        contours, _ = cv2.findContours(
            mask, cv2.RETR_EXTERNAL, cv2.CHAIN_APPROX_SIMPLE
        )

        if contours:
            largest = max(contours, key=cv2.contourArea)
            if cv2.contourArea(largest) >= args.min_area:
                (x, y, w, h) = cv2.boundingRect(largest)
                cv2.rectangle(frame, (x, y), (x + w, y + h), (0, 255, 255), 2)
                cv2.putText(
                    frame,
                    "Yellow object",
                    (x, y - 10),
                    cv2.FONT_HERSHEY_SIMPLEX,
                    0.7,
                    (0, 255, 255),
                    2,
                )

        cv2.imshow("Yellow Object Tracking - press q to quit", frame)
        if cv2.waitKey(1) & 0xFF == ord("q"):
            break

    cam.release()
    cv2.destroyAllWindows()


if __name__ == "__main__":
    main()
