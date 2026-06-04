import AVFoundation
import ComposeApp
import Foundation

final class NativeMediaDownloadBridge: NSObject, IosMediaDownloadBridge, AVAssetDownloadDelegate {
    static let shared = NativeMediaDownloadBridge()

    private let locationsKey = "NativeMediaDownloadBridge.locations"
    private let statesKey = "NativeMediaDownloadBridge.states"
    private var session: AVAssetDownloadURLSession!
    private var listener: IosMediaDownloadBridgeListener?
    private var locations: [String: String]
    private var states: [String: String]

    private override init() {
        locations = UserDefaults.standard.dictionary(forKey: locationsKey) as? [String: String] ?? [:]
        states = UserDefaults.standard.dictionary(forKey: statesKey) as? [String: String] ?? [:]

        super.init()

        let configuration = URLSessionConfiguration.background(
            withIdentifier: "com.witelokk.musicapp.media-cache.native"
        )
        configuration.allowsCellularAccess = true
        session = AVAssetDownloadURLSession(
            configuration: configuration,
            assetDownloadDelegate: self,
            delegateQueue: OperationQueue.main
        )

        session.getAllTasks { [weak self] tasks in
            tasks.forEach { task in
                guard let url = task.taskDescription else { return }
                self?.states[url] = "in_progress"
            }
            self?.persist()
        }
    }

    func startDownload(url: String, token: String?) {
        if cachedLocation(url: url) != nil {
            update(url: url, state: "cached", location: locations[url])
            return
        }

        guard let remoteUrl = URL(string: url) else {
            update(url: url, state: "failed", location: nil)
            return
        }

        var options: [String: Any] = [:]
        if let token, !token.isEmpty {
            options["AVURLAssetHTTPHeaderFieldsKey"] = [
                "Authorization": "Bearer \(token)"
            ]
        }

        let asset = AVURLAsset(url: remoteUrl, options: options.isEmpty ? nil : options)
        guard let task = session.makeAssetDownloadTask(
            asset: asset,
            assetTitle: stableHash(url),
            assetArtworkData: nil,
            options: nil
        ) else {
            update(url: url, state: "failed", location: nil)
            return
        }

        task.taskDescription = url
        update(url: url, state: "in_progress", location: nil)
        task.resume()
    }

    func cachedLocation(url: String) -> String? {
        guard let location = locations[url] else { return nil }

        if FileManager.default.fileExists(atPath: location) {
            return location
        }

        locations.removeValue(forKey: url)
        states[url] = "not_cached"
        persist()
        return nil
    }

    func cacheState(url: String) -> String {
        if cachedLocation(url: url) != nil {
            return "cached"
        }

        return states[url] ?? "not_cached"
    }

    func setListener(listener_ listener: IosMediaDownloadBridgeListener?) {
        self.listener = listener
    }

    func urlSession(
        _ session: URLSession,
        assetDownloadTask: AVAssetDownloadTask,
        didFinishDownloadingTo location: URL
    ) {
        guard let url = originalUrl(for: assetDownloadTask) else { return }
        update(url: url, state: "cached", location: location.path)
    }

    func urlSession(
        _ session: URLSession,
        task: URLSessionTask,
        didCompleteWithError error: Error?
    ) {
        guard let url = task.taskDescription else { return }

        if let error {
            NSLog("[IOS_MEDIA_CACHE] AVAsset download failed url=%@ error=%@", url, String(describing: error))
            update(url: url, state: "failed", location: nil)
            return
        }

        if cachedLocation(url: url) == nil {
            update(url: url, state: "failed", location: nil)
        }
    }

    func urlSession(
        _ session: URLSession,
        assetDownloadTask: AVAssetDownloadTask,
        didLoad timeRange: CMTimeRange,
        totalTimeRangesLoaded loadedTimeRanges: [NSValue],
        timeRangeExpectedToLoad: CMTimeRange
    ) {
        guard let url = originalUrl(for: assetDownloadTask) else { return }
        update(url: url, state: "in_progress", location: nil)
    }

    private func originalUrl(for task: AVAssetDownloadTask) -> String? {
        task.taskDescription ?? task.urlAsset.url.absoluteString
    }

    private func update(url: String, state: String, location: String?) {
        states[url] = state

        if let location {
            locations[url] = location
        } else if state != "in_progress" {
            locations.removeValue(forKey: url)
        }

        persist()
        listener?.onDownloadStateChanged(url: url, state: state, location: location)
        NSLog("[IOS_MEDIA_CACHE] native state url=%@ state=%@ location=%@", url, state, location ?? "nil")
    }

    private func persist() {
        UserDefaults.standard.set(locations, forKey: locationsKey)
        UserDefaults.standard.set(states, forKey: statesKey)
    }

    private func stableHash(_ value: String) -> String {
        var hash: UInt64 = 1_125_899_906_842_597
        for scalar in value.unicodeScalars {
            hash = hash &* 31 &+ UInt64(scalar.value)
        }
        return String(hash, radix: 16)
    }
}
