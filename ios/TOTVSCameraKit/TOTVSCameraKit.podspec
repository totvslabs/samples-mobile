
Pod::Spec.new do |s|
  s.name          = "TOTVSCameraKit"
  s.version       = "1.0.0"
  s.summary       = "TODO: description"

  s.homepage      = "https://github.com/totvslabs/samples-mobile.git"
  s.license       = 'MIT'
  s.author        = "TOTVS Labs <info@totvslabs.com> (https://www.totvslabs.com/)"
  s.source        = { :git => "https://github.com/totvslabs/samples-mobile.git", :tag => "v#{s.version}" }  

  s.requires_arc   = true
  s.swift_version  = '5.0'
  s.platform       = :ios, "13.0"
  

  s.subspec "Core" do |ss|
    ss.source_files = "Core/**/*.{swift,h,m}"
  end

  s.subspec "View" do |ss|
    ss.source_files = "View/**/*.{swift,h,m}"

    ss.dependency 'TOTVSCameraKit/Core'
  end

  s.subspec "Vision" do |ss|
    ss.source_files = "Vision/**/*.{swift,h,m}"

    ss.dependency 'TOTVSCameraKit/Core'
    ss.dependency 'GoogleMLKit/FaceDetection'
  end   

  s.default_subspecs = "Core", "View", "Vision"
end
